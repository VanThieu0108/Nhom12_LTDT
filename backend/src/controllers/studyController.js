import { z } from 'zod';
import { withTransaction } from '../config/db.js';
import * as progressModel from '../models/progressModel.js';
import * as vocabularyModel from '../models/vocabularyModel.js';
import { refreshStamina } from '../services/staminaService.js';
import { syncUserScore } from '../services/leaderboardService.js';
import { HttpError } from '../utils/httpError.js';

export const completeLevelSchema = z.object({
  answers: z.array(z.object({
    questionId: z.string().uuid(),
    answer: z.string().trim().min(1).max(100)
  })).min(1).max(30)
});

const zones = new Set(['listening', 'speaking', 'reading', 'writing']);

export function isPassingScore(correctAnswers, totalQuestions) {
  return correctAnswers * 5 >= totalQuestions * 4;
}

export async function getZones(request, response) {
  const [available, progress, stamina] = await Promise.all([
    vocabularyModel.listZones(null),
    progressModel.listUserProgress(null, request.user.id),
    withTransaction((client) => refreshStamina(client, request.user.id))
  ]);
  const unlockedZones = available.map((item) => ({
    ...item,
    unlocked: item.level === 1 || progress.some((entry) =>
      entry.zone === item.zone && entry.level === item.level - 1 && entry.passed
    )
  }));
  response.json({ zones: unlockedZones, progress, stamina });
}

export async function getQuestions(request, response) {
  const zone = request.params.zone.toLowerCase();
  const level = Number(request.params.level);
  if (!zones.has(zone) || !Number.isInteger(level) || level < 1 || level > 100) {
    throw new HttpError(400, 'Unknown zone or level.', 'invalid_level');
  }
  const result = await withTransaction(async (client) => {
    const unlocked = await progressModel.hasPassedLevel(client, request.user.id, zone, level);
    if (!unlocked) {
      throw new HttpError(403, 'Complete the previous level with at least 80% correct answers to unlock this level.', 'level_locked');
    }
    const stamina = await refreshStamina(client, request.user.id);
    const questions = await vocabularyModel.getQuestions(client, zone, level, 10);
    return { stamina, questions };
  });
  if (!result.questions.length) throw new HttpError(404, 'No questions are available for this level.', 'level_not_found');
  response.json({
    zone,
    level,
    stamina: result.stamina,
    questions: result.questions.map(({ answer: _answer, ...question }) => question)
  });
}

export async function completeLevel(request, response) {
  const zone = request.params.zone.toLowerCase();
  const level = Number(request.params.level);
  if (!zones.has(zone) || !Number.isInteger(level) || level < 1 || level > 100) {
    throw new HttpError(400, 'Unknown zone or level.', 'invalid_level');
  }
  const answers = request.body.answers;
  const ids = [...new Set(answers.map((answer) => answer.questionId))];
  if (ids.length !== answers.length) throw new HttpError(400, 'Duplicate question answers are not allowed.', 'duplicate_answers');

  const result = await withTransaction(async (client) => {
    const lockedUser = await client.query('SELECT id FROM users WHERE id = $1 FOR UPDATE', [request.user.id]);
    if (!lockedUser.rowCount) throw new HttpError(404, 'Account not found.', 'user_not_found');

    const prior = await client.query(
      'SELECT id, points, passed FROM user_progress WHERE user_id = $1 AND zone = $2 AND level = $3',
      [request.user.id, zone, level]
    );
    const stamina = await refreshStamina(client, request.user.id);
    if (prior.rows[0]?.passed) {
      return { alreadyCompleted: true, passed: true, points: prior.rows[0].points, stamina };
    }
    if (stamina.stamina < 1) throw new HttpError(429, 'You are out of stamina. Please wait for it to recharge.', 'insufficient_stamina');
    const unlocked = await progressModel.hasPassedLevel(client, request.user.id, zone, level);
    if (!unlocked) {
      throw new HttpError(403, 'Complete the previous level with at least 80% correct answers to unlock this level.', 'level_locked');
    }

    const questions = await vocabularyModel.findQuestionsByIds(client, ids);
    if (questions.length !== ids.length || questions.some((question) => question.zone !== zone || question.level !== level)) {
      throw new HttpError(400, 'Answers must belong to this level.', 'invalid_questions');
    }
    const byId = new Map(questions.map((question) => [question.id, question]));
    const correctAnswers = answers.reduce((count, item) => {
      const question = byId.get(item.questionId);
      const expected = zone === 'reading' ? question.translation : question.answer;
      return count + (expected.toLocaleLowerCase() === item.answer.trim().toLocaleLowerCase() ? 1 : 0);
    }, 0);
    const passed = isPassingScore(correctAnswers, answers.length);
    const points = passed ? correctAnswers * 10 : 0;

    const progress = await progressModel.createCompletion(client, {
      userId: request.user.id,
      zone,
      level,
      correctAnswers,
      totalQuestions: answers.length,
      points,
      passed
    });
    const gold = passed ? correctAnswers * 5 + 10 : 0;
    await client.query(
      `UPDATE users SET stamina = stamina - 1, coins = coins + $2,
       experience = experience + $3, trophy_points = trophy_points + $3,
       updated_at = NOW() WHERE id = $1`,
      [request.user.id, gold, points]
    );
    const updatedUser = await client.query(
      'SELECT stamina, coins, trophy_points FROM users WHERE id = $1',
      [request.user.id]
    );
    return {
      alreadyCompleted: false,
      passed,
      requiredCorrectAnswers: Math.ceil(answers.length * 0.8),
      progress,
      points,
      gold,
      stamina: updatedUser.rows[0]
    };
  });

  if (result.passed && !result.alreadyCompleted && result.stamina.trophy_points !== undefined) {
    await syncUserScore(request.user.id, result.stamina.trophy_points);
  }
  response.json(result);
}

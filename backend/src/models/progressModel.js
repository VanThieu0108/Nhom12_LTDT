import { queryWith } from './queryWith.js';

export async function createCompletion(client, {
  userId, zone, level, correctAnswers, totalQuestions, points, passed
}) {
  const result = await queryWith(client,
    `INSERT INTO user_progress
       (user_id, zone, level, correct_answers, total_questions, points, passed)
     VALUES ($1, $2, $3, $4, $5, $6, $7)
     ON CONFLICT (user_id, zone, level) DO UPDATE
       SET correct_answers = EXCLUDED.correct_answers,
           total_questions = EXCLUDED.total_questions,
           points = EXCLUDED.points,
           passed = EXCLUDED.passed,
           completed_at = NOW()
     RETURNING id, user_id, zone, level, correct_answers, total_questions,
               points, passed, completed_at`,
    [userId, zone, level, correctAnswers, totalQuestions, points, passed]
  );
  return result.rows[0] ?? null;
}

export async function hasPassedLevel(client, userId, zone, level) {
  if (level <= 1) return true;
  const result = await queryWith(client,
    `SELECT 1 FROM user_progress
     WHERE user_id = $1 AND zone = $2 AND level = $3 AND passed = TRUE`,
    [userId, zone, level - 1]
  );
  return result.rowCount > 0;
}

export async function listUserProgress(client, userId) {
  const result = await queryWith(client,
    `SELECT zone, level, correct_answers, total_questions, points, passed, completed_at
     FROM user_progress WHERE user_id = $1 ORDER BY completed_at DESC`,
    [userId]
  );
  return result.rows;
}

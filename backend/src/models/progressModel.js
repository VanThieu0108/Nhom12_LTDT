import { queryWith } from './queryWith.js';

export async function createCompletion(client, {
  userId, zone, level, correctAnswers, totalQuestions, points
}) {
  const result = await queryWith(client,
    `INSERT INTO user_progress
       (user_id, zone, level, correct_answers, total_questions, points)
     VALUES ($1, $2, $3, $4, $5, $6)
     ON CONFLICT (user_id, zone, level) DO NOTHING
     RETURNING id, user_id, zone, level, correct_answers, total_questions, points, completed_at`,
    [userId, zone, level, correctAnswers, totalQuestions, points]
  );
  return result.rows[0] ?? null;
}

export async function listUserProgress(client, userId) {
  const result = await queryWith(client,
    `SELECT zone, level, correct_answers, total_questions, points, completed_at
     FROM user_progress WHERE user_id = $1 ORDER BY completed_at DESC`,
    [userId]
  );
  return result.rows;
}

import { queryWith } from './queryWith.js';

export async function insertScore(client, { userId, gameType, score, distance, metadata }) {
  const result = await queryWith(client,
    `INSERT INTO minigame_scores (user_id, game_type, score, distance, metadata)
     VALUES ($1, $2, $3, $4, $5::jsonb)
     RETURNING id, game_type, score, distance, metadata, created_at`,
    [userId, gameType, score, distance, JSON.stringify(metadata)]
  );
  return result.rows[0];
}

export async function listRecentScores(client, userId, limit) {
  const result = await queryWith(client,
    `SELECT game_type, score, distance, created_at
     FROM minigame_scores WHERE user_id = $1
     ORDER BY created_at DESC LIMIT $2`,
    [userId, limit]
  );
  return result.rows;
}

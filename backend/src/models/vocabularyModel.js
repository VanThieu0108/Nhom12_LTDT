import { queryWith } from './queryWith.js';

export async function listZones(client) {
  const result = await queryWith(client,
    `SELECT zone, level, COUNT(*)::int AS question_count
     FROM vocabulary GROUP BY zone, level ORDER BY zone, level`
  );
  return result.rows;
}

export async function getQuestions(client, zone, level, limit) {
  const result = await queryWith(client,
    `SELECT id, prompt, answer, translation, choices, audio_url
     FROM vocabulary WHERE zone = $1 AND level = $2
     ORDER BY random() LIMIT $3`,
    [zone, level, limit]
  );
  return result.rows;
}

export async function findQuestionsByIds(client, ids) {
  const result = await queryWith(client,
    `SELECT id, zone, level, answer, translation FROM vocabulary WHERE id = ANY($1::uuid[])`,
    [ids]
  );
  return result.rows;
}

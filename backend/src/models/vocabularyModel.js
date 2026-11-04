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
    `SELECT id, prompt, answer, translation, choices, audio_url, image_url,
            phonetic, category
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

export async function listVocabularyLevels(client) {
  const result = await queryWith(client,
    `SELECT difficulty_level AS level, COUNT(*)::int AS word_count
     FROM vocabulary_words GROUP BY difficulty_level ORDER BY difficulty_level`
  );
  return result.rows;
}

export async function listVocabularyCategories(client, level) {
  const result = await queryWith(client,
    `SELECT category, COUNT(*)::int AS word_count
     FROM vocabulary_words WHERE ($1::int IS NULL OR difficulty_level = $1)
     GROUP BY category ORDER BY category`,
    [level ?? null]
  );
  return result.rows;
}

export async function searchVocabulary(client, { level, category, zone, limit }) {
  const result = await queryWith(client,
    `SELECT id, difficulty_level AS level, category, word_en, meaning_vi,
            phonetic, audio_url, image_url
     FROM vocabulary_words
     WHERE difficulty_level = $1
       AND ($2::text IS NULL OR category ILIKE $2)
     ORDER BY random() LIMIT $3`,
    [level, category ?? null, limit]
  );
  return result.rows;
}

import { queryWith } from './queryWith.js';

export async function listForUser(client, userId) {
  const result = await queryWith(client,
    `SELECT i.id, i.egg_type, i.start_time, i.hatch_duration_seconds,
            i.claimed_at, p.name AS pet_name
     FROM incubators i
     LEFT JOIN pets p ON p.id = i.pet_id
     WHERE i.user_id = $1 ORDER BY i.created_at DESC LIMIT 20`,
    [userId]
  );
  return result.rows;
}

export async function getForUpdate(client, incubatorId, userId) {
  const result = await client.query(
    `SELECT i.id, i.egg_type, i.start_time, i.hatch_duration_seconds, i.claimed_at,
            EXTRACT(EPOCH FROM (NOW() - i.start_time))::int AS elapsed_seconds
     FROM incubators i
     WHERE i.id = $1 AND i.user_id = $2
     FOR UPDATE`,
    [incubatorId, userId]
  );
  return result.rows[0] ?? null;
}

export async function create(client, userId, eggType, durationSeconds) {
  const result = await client.query(
    `INSERT INTO incubators (user_id, egg_type, start_time, hatch_duration_seconds)
     VALUES ($1, $2, NOW(), $3)
     RETURNING id, egg_type, start_time, hatch_duration_seconds`,
    [userId, eggType, durationSeconds]
  );
  return result.rows[0];
}

export async function claim(client, incubatorId, petId) {
  await client.query(
    'UPDATE incubators SET claimed_at = NOW(), pet_id = $2 WHERE id = $1',
    [incubatorId, petId]
  );
}

export async function addPetToInventory(client, userId, petId) {
  const result = await client.query(
    `INSERT INTO user_pets (user_id, pet_id, source)
     VALUES ($1, $2, 'incubator')
     ON CONFLICT (user_id, pet_id) DO NOTHING
     RETURNING id`,
    [userId, petId]
  );
  return result.rowCount === 1;
}

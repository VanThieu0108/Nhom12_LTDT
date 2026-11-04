import { query as dbQuery } from '../config/db.js';

const queryWith = (client, text, values) => client ? client.query(text, values) : dbQuery(text, values);

export async function createUser(client, { explorerName, normalizedName, pinHash }) {
  const result = await queryWith(client,
    `INSERT INTO users (explorer_name, explorer_name_normalized, pin_hash)
     VALUES ($1, $2, $3)
     RETURNING id, explorer_name, coins, stamina, trophy_points, created_at`,
    [explorerName, normalizedName, pinHash]
  );
  return result.rows[0];
}

export async function findByName(client, normalizedName) {
  const result = await queryWith(client,
    `SELECT id, explorer_name, explorer_name_normalized, pin_hash, coins, stamina, trophy_points
     FROM users WHERE explorer_name_normalized = $1 AND deleted_at IS NULL`,
    [normalizedName]
  );
  return result.rows[0] ?? null;
}

export async function findPublicUser(client, id) {
  const result = await queryWith(client,
    `SELECT id, explorer_name, coins, stamina, experience, trophy_points, created_at
     FROM users WHERE id = $1 AND deleted_at IS NULL`,
    [id]
  );
  return result.rows[0] ?? null;
}

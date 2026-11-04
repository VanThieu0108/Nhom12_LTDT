import { readdir, readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { pool, withTransaction } from '../config/db.js';

const directory = join(dirname(fileURLToPath(import.meta.url)), '../models/migrations');

try {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS schema_migrations (
      name TEXT PRIMARY KEY,
      applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    )
  `);
  const files = (await readdir(directory)).filter((file) => file.endsWith('.sql')).sort();
  for (const file of files) {
    const alreadyApplied = await pool.query('SELECT 1 FROM schema_migrations WHERE name = $1', [file]);
    if (alreadyApplied.rowCount) continue;
    const sql = await readFile(join(directory, file), 'utf8');
    await withTransaction(async (client) => {
      await client.query(sql);
      await client.query('INSERT INTO schema_migrations (name) VALUES ($1)', [file]);
    });
    console.log(`Applied migration ${file}`);
  }
} catch (error) {
  console.error('Database migration failed', error);
  process.exitCode = 1;
} finally {
  await pool.end();
}

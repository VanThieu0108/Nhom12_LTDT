import { readFile, stat } from 'node:fs/promises';
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import { z } from 'zod';
import { pool, withTransaction } from '../config/db.js';
import { csvObjects } from '../utils/csv.js';

const rowSchema = z.object({
  word_en: z.string().trim().min(1).max(100),
  meaning_vi: z.string().trim().min(1).max(100),
  difficulty_level: z.coerce.number().int().min(1).max(100),
  category: z.string().trim().min(1).max(50),
  phonetic: z.string().trim().max(100).optional().transform((value) => value || null),
  audio_url: z.string().trim().max(2048).url().optional().or(z.literal('')).transform((value) => value || null),
  image_url: z.string().trim().max(2048).url().optional().or(z.literal('')).transform((value) => value || null)
});
const requiredHeaders = ['word_en', 'meaning_vi', 'difficulty_level', 'category'];
const optionalHeaders = ['phonetic', 'audio_url', 'image_url'];

export function parseVocabularyCsv(text) {
  const objects = csvObjects(text, requiredHeaders);
  const supportedHeaders = new Set([...requiredHeaders, ...optionalHeaders]);
  const unknown = Object.keys(objects[0] ?? {}).filter((header) => !supportedHeaders.has(header));
  if (unknown.length) throw new Error(`CSV contains unsupported columns: ${unknown.join(', ')}.`);
  if (!objects.length) throw new Error('CSV has a header but no vocabulary rows.');
  if (objects.length > 100_000) throw new Error('CSV may contain at most 100,000 rows per import.');

  return objects.map((row, index) => {
    const parsed = rowSchema.safeParse(row);
    if (!parsed.success) {
      const details = parsed.error.issues.map((issue) => `${issue.path.join('.')}: ${issue.message}`).join('; ');
      throw new Error(`Invalid CSV row ${index + 2}: ${details}`);
    }
    return parsed.data;
  });
}

export async function importVocabulary(rows) {
  return withTransaction(async (client) => {
    let inserted = 0;
    const chunkSize = 500;
    for (let start = 0; start < rows.length; start += chunkSize) {
      const chunk = rows.slice(start, start + chunkSize);
      const values = [];
      const placeholders = chunk.map((word, index) => {
        const base = index * 7;
        values.push(
          word.word_en,
          word.meaning_vi,
          word.difficulty_level,
          word.category,
          word.phonetic,
          word.audio_url,
          word.image_url
        );
        return `($${base + 1}, $${base + 2}, $${base + 3}, $${base + 4}, $${base + 5}, $${base + 6}, $${base + 7})`;
      });
      const result = await client.query(
        `INSERT INTO vocabulary_words
          (word_en, meaning_vi, difficulty_level, category, phonetic, audio_url, image_url)
         VALUES ${placeholders.join(', ')}
         ON CONFLICT DO NOTHING`,
        values
      );
      inserted += result.rowCount;
    }
    return { total: rows.length, inserted, skipped: rows.length - inserted };
  });
}

async function main() {
  const separator = process.argv.indexOf('--file');
  const file = separator >= 0 ? process.argv[separator + 1] : null;
  if (!file || file.startsWith('--')) {
    throw new Error('Usage: npm run import:vocabulary -- --file <path-to-csv>');
  }
  const fileInfo = await stat(file);
  if (!fileInfo.isFile()) throw new Error('CSV path must reference a file.');
  if (fileInfo.size > 25 * 1024 * 1024) throw new Error('CSV file must be 25 MB or smaller.');

  const rows = parseVocabularyCsv(await readFile(file, 'utf8'));
  const result = await importVocabulary(rows);
  console.log(`Vocabulary import complete: ${result.inserted} inserted, ${result.skipped} duplicates skipped (${result.total} rows).`);
}

if (process.argv[1] && import.meta.url === pathToFileURL(resolve(process.argv[1])).href) {
  main()
    .catch((error) => {
      console.error(`Vocabulary import failed: ${error.message}`);
      process.exitCode = 1;
    })
    .finally(() => pool.end());
}

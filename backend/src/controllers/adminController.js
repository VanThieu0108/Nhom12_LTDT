import { createHash, timingSafeEqual } from 'node:crypto';
import { z } from 'zod';
import { query } from '../config/db.js';
import { env } from '../config/env.js';
import { HttpError } from '../utils/httpError.js';
import { signAdminToken } from '../utils/jwt.js';

const uuidSchema = z.string().uuid();
const loginSchema = z.object({
  username: z.string().min(1).max(64),
  password: z.string().min(1).max(256)
});
const vocabularySchema = z.object({
  zone: z.enum(['listening', 'speaking', 'reading', 'writing']),
  level: z.number().int().min(1).max(100),
  prompt: z.string().trim().min(1).max(500),
  answer: z.string().trim().min(1).max(100),
  translation: z.string().trim().min(1).max(100),
  choices: z.array(z.string().trim().min(1).max(100)).min(2).max(8),
  audioUrl: z.string().trim().max(2048).url().nullable().optional()
});
const petSchema = z.object({
  name: z.string().trim().min(1).max(48),
  species: z.string().trim().min(1).max(48),
  rarity: z.enum(['common', 'rare', 'epic', 'legendary']),
  imageUrl: z.string().trim().max(2048).url().nullable().optional()
});
const paginationSchema = z.object({
  search: z.string().trim().max(100).default(''),
  limit: z.coerce.number().int().min(1).max(100).default(50),
  offset: z.coerce.number().int().min(0).max(100_000).default(0)
});

export async function login(request, response) {
  const { username, password } = loginSchema.parse(request.body);
  if (!env.ADMIN_USERNAME || !env.ADMIN_PASSWORD) {
    throw new HttpError(503, 'Admin login is not configured on this server.', 'admin_not_configured');
  }
  if (!secureEqual(username, env.ADMIN_USERNAME) || !secureEqual(password, env.ADMIN_PASSWORD)) {
    throw new HttpError(401, 'Incorrect admin username or password.', 'invalid_credentials');
  }
  response.json({
    accessToken: await signAdminToken(env.ADMIN_USERNAME),
    tokenType: 'Bearer',
    expiresIn: env.JWT_TTL
  });
}

export async function overview(_request, response) {
  const [counts, recentScores] = await Promise.all([
    query(`
      SELECT
        (SELECT COUNT(*)::int FROM users WHERE deleted_at IS NULL) AS users,
        (SELECT COUNT(*)::int FROM vocabulary) AS questions,
        (SELECT COUNT(*)::int FROM pets) AS pets,
        (SELECT COUNT(*)::int FROM user_progress) AS completions,
        (SELECT COUNT(*)::int FROM minigame_scores) AS minigame_scores
    `),
    query(`
      SELECT s.id, s.game_type, s.score, s.distance, s.created_at,
             u.explorer_name
      FROM minigame_scores s
      JOIN users u ON u.id = s.user_id
      ORDER BY s.created_at DESC
      LIMIT 8
    `)
  ]);
  response.json({ counts: counts.rows[0], recentScores: recentScores.rows });
}

export async function listUsers(request, response) {
  const { search, limit, offset } = paginationSchema.parse(request.query);
  const result = await query(
    `SELECT u.id, u.explorer_name, u.coins, u.stamina, u.experience,
            u.trophy_points, u.created_at,
            COUNT(p.id)::int AS completed_levels
     FROM users u
     LEFT JOIN user_progress p ON p.user_id = u.id
     WHERE u.deleted_at IS NULL
       AND ($1 = '' OR u.explorer_name ILIKE '%' || $1 || '%')
     GROUP BY u.id
     ORDER BY u.created_at DESC
     LIMIT $2 OFFSET $3`,
    [search, limit, offset]
  );
  response.json({ users: result.rows, limit, offset });
}

export async function listVocabulary(request, response) {
  const { search, limit, offset } = paginationSchema.parse(request.query);
  const zone = request.query.zone;
  if (zone && !['listening', 'speaking', 'reading', 'writing'].includes(zone)) {
    throw new HttpError(400, 'Unknown learning zone.', 'invalid_zone');
  }
  const result = await query(
    `SELECT id, zone, level, prompt, answer, translation, choices, audio_url, created_at
     FROM vocabulary
     WHERE ($1::text IS NULL OR zone = $1)
       AND ($2 = '' OR prompt ILIKE '%' || $2 || '%' OR answer ILIKE '%' || $2 || '%' OR translation ILIKE '%' || $2 || '%')
     ORDER BY zone, level, created_at DESC
     LIMIT $3 OFFSET $4`,
    [zone || null, search, limit, offset]
  );
  response.json({ questions: result.rows, limit, offset });
}

export async function createVocabulary(request, response) {
  const data = vocabularySchema.parse(request.body);
  const result = await query(
    `INSERT INTO vocabulary (zone, level, prompt, answer, translation, choices, audio_url)
     VALUES ($1, $2, $3, $4, $5, $6, $7)
     RETURNING id, zone, level, prompt, answer, translation, choices, audio_url, created_at`,
    [data.zone, data.level, data.prompt, data.answer, data.translation, JSON.stringify(data.choices), data.audioUrl ?? null]
  );
  response.status(201).json({ question: result.rows[0] });
}

export async function updateVocabulary(request, response) {
  const id = parseId(request.params.id);
  const data = vocabularySchema.parse(request.body);
  const result = await query(
    `UPDATE vocabulary
     SET zone = $2, level = $3, prompt = $4, answer = $5, translation = $6,
         choices = $7, audio_url = $8
     WHERE id = $1
     RETURNING id, zone, level, prompt, answer, translation, choices, audio_url, created_at`,
    [id, data.zone, data.level, data.prompt, data.answer, data.translation, JSON.stringify(data.choices), data.audioUrl ?? null]
  );
  if (!result.rowCount) throw new HttpError(404, 'Question not found.', 'question_not_found');
  response.json({ question: result.rows[0] });
}

export async function deleteVocabulary(request, response) {
  const result = await query('DELETE FROM vocabulary WHERE id = $1', [parseId(request.params.id)]);
  if (!result.rowCount) throw new HttpError(404, 'Question not found.', 'question_not_found');
  response.status(204).end();
}

export async function listPets(_request, response) {
  const result = await query(
    `SELECT p.id, p.name, p.species, p.rarity, p.image_url, p.created_at,
            COUNT(up.id)::int AS owners
     FROM pets p
     LEFT JOIN user_pets up ON up.pet_id = p.id
     GROUP BY p.id
     ORDER BY CASE p.rarity WHEN 'legendary' THEN 1 WHEN 'epic' THEN 2 WHEN 'rare' THEN 3 ELSE 4 END, p.name`
  );
  response.json({ pets: result.rows });
}

export async function createPet(request, response) {
  const data = petSchema.parse(request.body);
  const result = await query(
    `INSERT INTO pets (name, species, rarity, image_url)
     VALUES ($1, $2, $3, $4)
     RETURNING id, name, species, rarity, image_url, created_at`,
    [data.name, data.species, data.rarity, data.imageUrl ?? null]
  );
  response.status(201).json({ pet: result.rows[0] });
}

export async function updatePet(request, response) {
  const id = parseId(request.params.id);
  const data = petSchema.parse(request.body);
  const result = await query(
    `UPDATE pets SET name = $2, species = $3, rarity = $4, image_url = $5
     WHERE id = $1
     RETURNING id, name, species, rarity, image_url, created_at`,
    [id, data.name, data.species, data.rarity, data.imageUrl ?? null]
  );
  if (!result.rowCount) throw new HttpError(404, 'Pet not found.', 'pet_not_found');
  response.json({ pet: result.rows[0] });
}

export async function deletePet(request, response) {
  const result = await query('DELETE FROM pets WHERE id = $1', [parseId(request.params.id)]);
  if (!result.rowCount) throw new HttpError(404, 'Pet not found.', 'pet_not_found');
  response.status(204).end();
}

function parseId(value) {
  const parsed = uuidSchema.safeParse(value);
  if (!parsed.success) throw new HttpError(400, 'Invalid resource ID.', 'invalid_id');
  return parsed.data;
}

function secureEqual(left, right) {
  const leftHash = createHash('sha256').update(left).digest();
  const rightHash = createHash('sha256').update(right).digest();
  return timingSafeEqual(leftHash, rightHash);
}

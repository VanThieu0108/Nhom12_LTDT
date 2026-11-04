import { z } from 'zod';
import { withTransaction } from '../config/db.js';
import { env } from '../config/env.js';
import * as petModel from '../models/petModel.js';
import * as incubatorModel from '../models/incubatorModel.js';
import { HttpError } from '../utils/httpError.js';

export const incubateSchema = z.object({
  eggType: z.enum(['common', 'rare', 'epic']).default('common')
});

const EGG_COST = { common: 50, rare: 100, epic: 200 };

export async function collection(request, response) {
  const [pets, incubators] = await Promise.all([
    petModel.listPets(null, request.user.id),
    incubatorModel.listForUser(null, request.user.id)
  ]);
  response.json({ pets, incubators });
}

export async function startIncubation(request, response) {
  const { eggType } = request.body;
  const incubator = await withTransaction(async (client) => {
    const user = await client.query('SELECT id, coins FROM users WHERE id = $1 FOR UPDATE', [request.user.id]);
    if (!user.rowCount) throw new HttpError(404, 'Account not found.', 'user_not_found');
    const existing = await client.query(
      'SELECT id FROM incubators WHERE user_id = $1 AND claimed_at IS NULL',
      [request.user.id]
    );
    if (existing.rowCount) throw new HttpError(409, 'An egg is already incubating.', 'incubator_busy');
    const cost = EGG_COST[eggType];
    if (user.rows[0].coins < cost) throw new HttpError(409, 'Not enough coins for this egg.', 'insufficient_coins');
    await client.query('UPDATE users SET coins = coins - $2, updated_at = NOW() WHERE id = $1', [request.user.id, cost]);
    return incubatorModel.create(client, request.user.id, eggType, env.INCUBATION_SECONDS);
  });
  response.status(201).json({ incubator });
}

export async function claimHatchedPet(request, response) {
  if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(request.params.id)) {
    throw new HttpError(400, 'Invalid incubator ID.', 'invalid_incubator_id');
  }
  const pet = await withTransaction(async (client) => {
    const incubator = await incubatorModel.getForUpdate(client, request.params.id, request.user.id);
    if (!incubator) throw new HttpError(404, 'Incubator not found.', 'incubator_not_found');
    if (incubator.claimed_at) throw new HttpError(409, 'This egg has already been claimed.', 'already_claimed');
    if (incubator.elapsed_seconds < incubator.hatch_duration_seconds) {
      throw new HttpError(409, 'This egg is still incubating.', 'not_ready', {
        remainingSeconds: incubator.hatch_duration_seconds - incubator.elapsed_seconds
      });
    }
    const rarities = {
      common: ['common', 'rare'],
      rare: ['rare', 'epic'],
      epic: ['epic', 'legendary']
    };
    const selected = await client.query(
      `SELECT id, name, species, rarity, image_url FROM pets
       WHERE rarity = ANY($1::text[]) ORDER BY random() LIMIT 1`,
      [rarities[incubator.egg_type]]
    );
    if (!selected.rowCount) throw new Error('No pets configured for this egg rarity.');
    const pet = selected.rows[0];
    await incubatorModel.claim(client, incubator.id, pet.id);
    const newlyUnlocked = await incubatorModel.addPetToInventory(client, request.user.id, pet.id);
    return { ...pet, newlyUnlocked };
  });
  response.json({ pet });
}

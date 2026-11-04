import bcrypt from 'bcryptjs';
import { z } from 'zod';
import { withTransaction } from '../config/db.js';
import * as userModel from '../models/userModel.js';
import { syncUserScore } from '../services/leaderboardService.js';
import { HttpError } from '../utils/httpError.js';
import { signAccessToken } from '../utils/jwt.js';

export const registerSchema = z.object({
  explorerName: z.string().trim().min(2).max(32).regex(/^[\p{L}\p{N} _-]+$/u),
  pin: z.string().regex(/^\d{4,12}$/)
});

export const loginSchema = z.object({
  explorerName: z.string().trim().min(1).max(32),
  pin: z.string().regex(/^\d{4,12}$/)
});

const DUMMY_PIN_HASH = bcrypt.hashSync('not-a-real-explorer-pin', 12);

export async function register(request, response) {
  const { explorerName, pin } = request.body;
  const normalizedName = explorerName.toLocaleLowerCase('en-US');
  const pinHash = await bcrypt.hash(pin, 12);
  const user = await withTransaction((client) =>
    userModel.createUser(client, { explorerName, normalizedName, pinHash })
  );
  await syncUserScore(user.id, user.trophy_points);
  const accessToken = await signAccessToken(user);
  response.status(201).json({ user: publicUser(user), accessToken, tokenType: 'Bearer' });
}

export async function login(request, response) {
  const { explorerName, pin } = request.body;
  const user = await userModel.findByName(null, explorerName.trim().toLocaleLowerCase('en-US'));
  const validPin = await bcrypt.compare(pin, user?.pin_hash ?? DUMMY_PIN_HASH);
  if (!user || !validPin) {
    throw new HttpError(401, 'Explorer name or PIN is incorrect.', 'invalid_credentials');
  }
  const accessToken = await signAccessToken(user);
  response.json({ user: publicUser(user), accessToken, tokenType: 'Bearer' });
}

export async function me(request, response) {
  const user = await userModel.findPublicUser(null, request.user.id);
  if (!user) throw new HttpError(404, 'Account not found.', 'user_not_found');
  response.json({ user });
}

function publicUser(user) {
  return {
    id: user.id,
    explorerName: user.explorer_name,
    coins: user.coins,
    stamina: user.stamina,
    trophyPoints: user.trophy_points
  };
}

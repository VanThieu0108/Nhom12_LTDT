import { z } from 'zod';
import * as gameScoreModel from '../models/gameScoreModel.js';
import { HttpError } from '../utils/httpError.js';

export const scoreSchema = z.object({
  gameType: z.enum(['word_match', 'runner', 'meteor']),
  score: z.number().int().min(0).max(100_000),
  distance: z.number().int().min(0).max(10_000_000).default(0),
  metadata: z.record(z.string(), z.unknown()).default({})
});

export async function saveScore(request, response) {
  const { gameType, score, distance, metadata } = request.body;
  if (gameType !== 'runner' && distance !== 0) {
    throw new HttpError(400, 'Distance is only valid for runner scores.', 'invalid_score');
  }
  const saved = await gameScoreModel.insertScore(null, {
    userId: request.user.id,
    gameType,
    score,
    distance,
    metadata
  });
  response.status(201).json({ saved });
}

export async function listScores(request, response) {
  const scores = await gameScoreModel.listRecentScores(null, request.user.id, 50);
  response.json({ scores });
}

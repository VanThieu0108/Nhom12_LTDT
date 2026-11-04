import { getLeaderboard } from '../services/leaderboardService.js';
import { HttpError } from '../utils/httpError.js';

export async function leaderboard(request, response) {
  const value = request.query.limit ?? '20';
  const limit = typeof value === 'string' ? Number(value) : Number.NaN;
  if (!Number.isInteger(limit) || limit < 1 || limit > 100) {
    throw new HttpError(400, 'Limit must be an integer between 1 and 100.', 'invalid_limit');
  }
  const result = await getLeaderboard(limit, request.user?.id);
  response.json(result);
}

import { query } from '../config/db.js';
import { HttpError } from '../utils/httpError.js';
import { verifyAccessToken } from '../utils/jwt.js';

export async function requireAuth(request, _response, next) {
  const authorization = request.get('authorization') ?? '';
  const match = authorization.match(/^Bearer\s+(.+)$/i);
  if (!match) {
    next(new HttpError(401, 'Authentication required.', 'unauthorized'));
    return;
  }
  let payload;
  try {
    payload = await verifyAccessToken(match[1]);
  } catch {
    next(new HttpError(401, 'Invalid or expired token.', 'unauthorized'));
    return;
  }
  try {
    const result = await query(
      'SELECT id, explorer_name, coins, stamina, trophy_points FROM users WHERE id = $1 AND deleted_at IS NULL',
      [payload.sub]
    );
    if (!result.rowCount) {
      next(new HttpError(401, 'Account is unavailable.', 'unauthorized'));
      return;
    }
    request.user = result.rows[0];
    next();
  } catch (error) {
    next(error);
  }
}

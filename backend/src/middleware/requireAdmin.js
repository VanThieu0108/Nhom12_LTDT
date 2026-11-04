import { HttpError } from '../utils/httpError.js';
import { verifyAccessToken } from '../utils/jwt.js';

export async function requireAdmin(request, _response, next) {
  const authorization = request.get('authorization') ?? '';
  const match = authorization.match(/^Bearer\s+(.+)$/i);
  if (!match) {
    next(new HttpError(401, 'Admin authentication required.', 'unauthorized'));
    return;
  }

  try {
    const payload = await verifyAccessToken(match[1]);
    if (payload.role !== 'admin') {
      next(new HttpError(403, 'Administrator access required.', 'forbidden'));
      return;
    }
    request.admin = { username: payload.sub };
    next();
  } catch {
    next(new HttpError(401, 'Invalid or expired admin session.', 'unauthorized'));
  }
}

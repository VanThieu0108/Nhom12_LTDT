import { ZodError } from 'zod';
import { HttpError } from '../utils/httpError.js';

export function notFoundHandler(_request, _response, next) {
  next(new HttpError(404, 'Route not found.', 'not_found'));
}

export function errorHandler(error, _request, response, _next) {
  if (error instanceof ZodError) {
    response.status(400).json({ error: { code: 'validation_error', message: 'Invalid request.' } });
    return;
  }
  if (error.code === '23505') {
    response.status(409).json({ error: { code: 'conflict', message: 'That value is already in use.' } });
    return;
  }
  if (error.code === '23503') {
    response.status(409).json({ error: { code: 'resource_in_use', message: 'This item is being used and cannot be deleted.' } });
    return;
  }
  const status = error instanceof HttpError ? error.status : 500;
  if (status >= 500) console.error(error);
  const body = {
    error: {
      code: error instanceof HttpError ? error.code : 'internal_error',
      message: status >= 500 ? 'An unexpected server error occurred.' : error.message
    }
  };
  if (status < 500 && error.details) body.error.details = error.details;
  response.status(status).json(body);
}

import { HttpError } from '../utils/httpError.js';

export function validateBody(schema) {
  return (request, _response, next) => {
    const result = schema.safeParse(request.body);
    if (!result.success) {
      next(new HttpError(400, 'Request body is invalid.', 'validation_error'));
      return;
    }
    request.body = result.data;
    next();
  };
}

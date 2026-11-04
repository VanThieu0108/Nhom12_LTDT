import { z } from 'zod';
import * as vocabularyModel from '../models/vocabularyModel.js';
import { HttpError } from '../utils/httpError.js';

const querySchema = z.object({
  level: z.coerce.number().int().min(1).max(100),
  category: z.string().trim().min(1).max(50).optional(),
  limit: z.coerce.number().int().min(1).max(50).default(10)
});

const levelFilterSchema = z.object({
  level: z.coerce.number().int().min(1).max(100).optional()
});

export async function levels(_request, response) {
  const data = await vocabularyModel.listVocabularyLevels(null);
  response.json({ success: true, data });
}

export async function categories(request, response) {
  const { level } = levelFilterSchema.parse(request.query);
  const data = await vocabularyModel.listVocabularyCategories(null, level);
  response.json({ success: true, data });
}

export async function list(request, response) {
  const { level, category, limit } = querySchema.parse(request.query);
  const data = await vocabularyModel.searchVocabulary(null, { level, category, limit });
  if (!data.length) {
    throw new HttpError(404, 'No vocabulary is available for these filters.', 'vocabulary_not_found');
  }
  response.json({ success: true, data });
}

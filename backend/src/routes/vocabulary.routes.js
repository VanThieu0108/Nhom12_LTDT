import { Router } from 'express';
import { asyncHandler } from '../utils/asyncHandler.js';
import * as controller from '../controllers/vocabularyController.js';

const router = Router();
router.get('/levels', asyncHandler(controller.levels));
router.get('/categories', asyncHandler(controller.categories));
router.get('/', asyncHandler(controller.list));

export default router;

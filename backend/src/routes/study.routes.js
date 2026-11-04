import { Router } from 'express';
import { requireAuth } from '../middleware/auth.js';
import { validateBody } from '../middleware/validate.js';
import { asyncHandler } from '../utils/asyncHandler.js';
import * as controller from '../controllers/studyController.js';

const router = Router();
router.use(requireAuth);
router.get('/zones', asyncHandler(controller.getZones));
router.get('/zones/:zone/levels/:level/questions', asyncHandler(controller.getQuestions));
router.post(
  '/zones/:zone/levels/:level/complete',
  validateBody(controller.completeLevelSchema),
  asyncHandler(controller.completeLevel)
);

export default router;

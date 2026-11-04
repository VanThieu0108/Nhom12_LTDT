import { Router } from 'express';
import { requireAuth } from '../middleware/auth.js';
import { validateBody } from '../middleware/validate.js';
import { asyncHandler } from '../utils/asyncHandler.js';
import * as controller from '../controllers/minigameController.js';

const router = Router();
router.use(requireAuth);
router.post('/scores', validateBody(controller.scoreSchema), asyncHandler(controller.saveScore));
router.get('/scores', asyncHandler(controller.listScores));

export default router;

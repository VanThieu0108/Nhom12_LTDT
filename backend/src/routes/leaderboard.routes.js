import { Router } from 'express';
import { requireAuth } from '../middleware/auth.js';
import { asyncHandler } from '../utils/asyncHandler.js';
import * as controller from '../controllers/leaderboardController.js';

const router = Router();
router.get('/', requireAuth, asyncHandler(controller.leaderboard));

export default router;

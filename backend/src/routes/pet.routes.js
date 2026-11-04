import { Router } from 'express';
import { requireAuth } from '../middleware/auth.js';
import { validateBody } from '../middleware/validate.js';
import { asyncHandler } from '../utils/asyncHandler.js';
import * as controller from '../controllers/petController.js';

const router = Router();
router.use(requireAuth);
router.get('/', asyncHandler(controller.collection));
router.post('/incubators', validateBody(controller.incubateSchema), asyncHandler(controller.startIncubation));
router.post('/incubators/:id/claim', asyncHandler(controller.claimHatchedPet));

export default router;

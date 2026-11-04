import { Router } from 'express';
import { rateLimit } from 'express-rate-limit';
import { RedisStore } from 'rate-limit-redis';
import { redis } from '../config/redis.js';
import { asyncHandler } from '../utils/asyncHandler.js';
import { requireAdmin } from '../middleware/requireAdmin.js';
import * as controller from '../controllers/adminController.js';

const router = Router();
const loginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 5,
  store: new RedisStore({
    prefix: 'rate-limit:admin-login:',
    sendCommand: (command, ...args) => redis.call(command, ...args)
  }),
  standardHeaders: 'draft-8',
  legacyHeaders: false,
  message: { error: { code: 'rate_limited', message: 'Too many admin login attempts.' } }
});

router.post('/login', loginLimiter, asyncHandler(controller.login));
router.use(requireAdmin);
router.get('/overview', asyncHandler(controller.overview));
router.get('/words', asyncHandler(controller.listWords));
router.get('/users', asyncHandler(controller.listUsers));
router.get('/vocabulary', asyncHandler(controller.listVocabulary));
router.post('/vocabulary', asyncHandler(controller.createVocabulary));
router.put('/vocabulary/:id', asyncHandler(controller.updateVocabulary));
router.delete('/vocabulary/:id', asyncHandler(controller.deleteVocabulary));
router.get('/pets', asyncHandler(controller.listPets));
router.post('/pets', asyncHandler(controller.createPet));
router.put('/pets/:id', asyncHandler(controller.updatePet));
router.delete('/pets/:id', asyncHandler(controller.deletePet));

export default router;

import { Router } from 'express';
import { rateLimit } from 'express-rate-limit';
import { RedisStore } from 'rate-limit-redis';
import { redis } from '../config/redis.js';
import { asyncHandler } from '../utils/asyncHandler.js';
import { requireAuth } from '../middleware/auth.js';
import { validateBody } from '../middleware/validate.js';
import * as controller from '../controllers/authController.js';

const router = Router();
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 10,
  store: new RedisStore({
    prefix: 'rate-limit:auth:',
    sendCommand: (command, ...args) => redis.call(command, ...args)
  }),
  standardHeaders: 'draft-8',
  legacyHeaders: false,
  message: { error: { code: 'rate_limited', message: 'Too many authentication attempts.' } }
});

router.post('/register', authLimiter, validateBody(controller.registerSchema), asyncHandler(controller.register));
router.post('/login', authLimiter, validateBody(controller.loginSchema), asyncHandler(controller.login));
router.get('/me', requireAuth, asyncHandler(controller.me));

export default router;

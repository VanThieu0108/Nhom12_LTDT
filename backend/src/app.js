import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import { rateLimit } from 'express-rate-limit';
import { corsOrigins, env } from './config/env.js';
import authRoutes from './routes/auth.routes.js';
import studyRoutes from './routes/study.routes.js';
import minigameRoutes from './routes/minigame.routes.js';
import petRoutes from './routes/pet.routes.js';
import leaderboardRoutes from './routes/leaderboard.routes.js';
import adminRoutes from './routes/admin.routes.js';
import { notFoundHandler, errorHandler } from './middleware/errorHandler.js';
import { asyncHandler } from './utils/asyncHandler.js';
import { query } from './config/db.js';
import { redis } from './config/redis.js';
import { RedisStore } from 'rate-limit-redis';
import { HttpError } from './utils/httpError.js';
import { fileURLToPath } from 'node:url';

export function createApp() {
  const app = express();
  app.disable('x-powered-by');
  if (env.TRUST_PROXY) {
    const trustedHops = /^\d+$/.test(env.TRUST_PROXY) ? Number(env.TRUST_PROXY) : env.TRUST_PROXY;
    app.set('trust proxy', trustedHops);
  }
  app.use(helmet());
  app.use(cors({
    origin(origin, callback) {
      if (!origin || corsOrigins.length === 0 || corsOrigins.includes(origin)) {
        callback(null, true);
        return;
      }
      callback(new HttpError(403, 'Origin is not allowed by CORS.', 'cors_origin_denied'));
    },
    credentials: false
  }));
  app.use(express.json({ limit: '32kb' }));
  app.use('/admin', express.static(fileURLToPath(new URL('../admin/', import.meta.url))));
  app.use(rateLimit({
    windowMs: 60_000,
    limit: 120,
    store: new RedisStore({
      prefix: 'rate-limit:api:',
      sendCommand: (command, ...args) => redis.call(command, ...args)
    }),
    standardHeaders: 'draft-8',
    legacyHeaders: false
  }));

  app.get('/', (_request, response) => {
    response.json({
      name: 'Kids English Adventure API',
      status: 'running',
      health: '/health',
      admin: '/admin',
      endpoints: [
        '/api/auth',
        '/api/study',
        '/api/minigames',
        '/api/pets',
        '/api/leaderboard',
        '/api/admin'
      ]
    });
  });

  app.get('/health', asyncHandler(async (_request, response) => {
    await Promise.all([query('SELECT 1'), redis.ping()]);
    response.json({ status: 'ok' });
  }));
  app.use('/api/auth', authRoutes);
  app.use('/api/study', studyRoutes);
  app.use('/api/minigames', minigameRoutes);
  app.use('/api/pets', petRoutes);
  app.use('/api/leaderboard', leaderboardRoutes);
  app.use('/api/admin', adminRoutes);
  app.use(notFoundHandler);
  app.use(errorHandler);
  return app;
}

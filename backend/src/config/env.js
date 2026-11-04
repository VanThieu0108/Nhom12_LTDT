import 'dotenv/config';
import { z } from 'zod';

const schema = z.object({
  NODE_ENV: z.enum(['development', 'test', 'production']).default('development'),
  PORT: z.coerce.number().int().positive().default(3000),
  DATABASE_URL: z.string().min(1),
  DATABASE_SSL: z.string().optional().transform((value) => value === 'true'),
  REDIS_URL: z.string().min(1),
  JWT_SECRET: z.string().min(32),
  JWT_ISSUER: z.string().default('kids-english-adventure'),
  JWT_AUDIENCE: z.string().default('kids-english-mobile'),
  JWT_TTL: z.string().default('15m'),
  ADMIN_USERNAME: z.string().default(''),
  ADMIN_PASSWORD: z.string().default(''),
  CORS_ORIGINS: z.string().default(''),
  SOCKET_CORS_ORIGINS: z.string().default(''),
  TRUST_PROXY: z.string().default(''),
  STAMINA_MAX: z.coerce.number().int().positive().default(5),
  STAMINA_REGEN_SECONDS: z.coerce.number().int().positive().default(1800),
  INCUBATION_SECONDS: z.coerce.number().int().positive().default(600),
  MATCH_TROPHY_RANGE: z.coerce.number().int().nonnegative().default(250)
}).superRefine((value, context) => {
  if (value.NODE_ENV === 'production') {
    if (!value.ADMIN_USERNAME || value.ADMIN_PASSWORD.length < 16) {
      context.addIssue({ code: 'custom', path: ['ADMIN_PASSWORD'], message: 'Configure ADMIN_USERNAME and an ADMIN_PASSWORD of at least 16 characters.' });
    }
    if (!value.DATABASE_SSL) {
      context.addIssue({ code: 'custom', path: ['DATABASE_SSL'], message: 'DATABASE_SSL must be enabled in production.' });
    }
    if (!value.REDIS_URL.startsWith('rediss://')) {
      context.addIssue({ code: 'custom', path: ['REDIS_URL'], message: 'Use TLS for Redis in production.' });
    }
    const configuredOrigins = [value.CORS_ORIGINS, value.SOCKET_CORS_ORIGINS]
      .join(',')
      .split(',')
      .map((origin) => origin.trim())
      .filter(Boolean);
    if (!configuredOrigins.length || configuredOrigins.some((origin) => origin === '*')) {
      context.addIssue({ code: 'custom', path: ['CORS_ORIGINS'], message: 'Production CORS allow-lists are required.' });
    }
    for (const origin of configuredOrigins) {
      try {
        const parsed = new URL(origin);
        const localDevelopmentOrigin = parsed.hostname === 'localhost' || parsed.hostname === '127.0.0.1';
        if (parsed.origin !== origin || (parsed.protocol !== 'https:' && !(localDevelopmentOrigin && parsed.protocol === 'http:'))) {
          throw new Error('Invalid origin');
        }
      } catch {
        context.addIssue({ code: 'custom', path: ['CORS_ORIGINS'], message: `Invalid production CORS origin: ${origin}` });
      }
    }
  }
});

export const env = schema.parse(process.env);
export const corsOrigins = env.CORS_ORIGINS.split(',').map((item) => item.trim()).filter(Boolean);
export const socketCorsOrigins = env.SOCKET_CORS_ORIGINS
  .split(',')
  .map((item) => item.trim())
  .filter(Boolean);

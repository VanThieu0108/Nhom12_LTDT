import Redis from 'ioredis';
import { env } from './env.js';

export const redis = new Redis(env.REDIS_URL, {
  maxRetriesPerRequest: 3,
  enableReadyCheck: true,
  lazyConnect: true
});

redis.on('error', (error) => {
  console.error('Redis client error', error);
});

export async function connectRedis() {
  if (redis.status === 'wait') await redis.connect();
  await redis.ping();
}

export async function closeRedis() {
  if (redis.status !== 'end') await redis.quit();
}

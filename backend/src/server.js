import { createServer } from 'node:http';
import { Server } from 'socket.io';
import { createAdapter } from '@socket.io/redis-adapter';
import { createApp } from './app.js';
import { pool } from './config/db.js';
import { env, socketCorsOrigins } from './config/env.js';
import { closeRedis, connectRedis, redis } from './config/redis.js';
import { attachBattleSocket } from './sockets/battleSocket.js';

const app = createApp();
const httpServer = createServer(app);
const io = new Server(httpServer, {
  cors: {
    origin: socketCorsOrigins.length ? socketCorsOrigins : false,
    methods: ['GET', 'POST']
  },
  maxHttpBufferSize: 16_384
});

let publisher;
let subscriber;

async function start() {
  await pool.query('SELECT 1');
  await connectRedis();
  publisher = redis.duplicate();
  subscriber = redis.duplicate();
  await Promise.all([publisher.ping(), subscriber.ping()]);
  io.adapter(createAdapter(publisher, subscriber));
  attachBattleSocket(io);
  await new Promise((resolve, reject) => {
    httpServer.once('error', reject);
    httpServer.listen(env.PORT, resolve);
  });
  console.log(`Kids English API listening on port ${env.PORT}`);
}

let shuttingDown = false;
async function shutdown(signal) {
  if (shuttingDown) return;
  shuttingDown = true;
  console.log(`Received ${signal}; shutting down.`);
  const forceExit = setTimeout(() => process.exit(1), 10_000);
  forceExit.unref();
  await new Promise((resolve) => io.close(resolve));
  await Promise.all([publisher?.quit(), subscriber?.quit()]);
  await closeRedis();
  await pool.end();
  clearTimeout(forceExit);
}

process.once('SIGINT', () => shutdown('SIGINT').then(() => process.exit(0)));
process.once('SIGTERM', () => shutdown('SIGTERM').then(() => process.exit(0)));

start().catch(async (error) => {
  console.error('Server startup failed', error);
  await shutdown('startup failure');
  process.exit(1);
});

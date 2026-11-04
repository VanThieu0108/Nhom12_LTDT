import { randomUUID } from 'node:crypto';
import { z } from 'zod';
import { query } from '../config/db.js';
import { redis } from '../config/redis.js';
import { env } from '../config/env.js';
import { verifyAccessToken } from '../utils/jwt.js';

const QUEUE_KEY = 'battle:queue:trophies';
const SOCKETS_KEY = 'battle:queue:sockets';
const RELEASE_LOCK_SCRIPT = `
if redis.call('GET', KEYS[1]) == ARGV[1] then
  return redis.call('DEL', KEYS[1])
end
return 0
`;
const answerSchema = z.object({
  questionIndex: z.number().int().nonnegative(),
  answer: z.string().trim().min(1).max(100)
});

const MATCH_SCRIPT = `
local queue = KEYS[1]
local sockets = KEYS[2]
local activePrefix = ARGV[1]
local userId = ARGV[2]
local trophies = tonumber(ARGV[3])
local socketId = ARGV[4]
local tolerance = tonumber(ARGV[5])
if redis.call('EXISTS', activePrefix .. userId) == 1 then
  return {'ACTIVE'}
end
redis.call('ZADD', queue, trophies, userId)
redis.call('HSET', sockets, userId, socketId)
local candidates = redis.call('ZRANGEBYSCORE', queue, trophies - tolerance, trophies + tolerance, 'WITHSCORES')
local bestId = nil
local bestDifference = nil
for i = 1, #candidates, 2 do
  local candidate = candidates[i]
  if candidate ~= userId then
    local candidateSocket = redis.call('HGET', sockets, candidate)
    if not candidateSocket or redis.call('EXISTS', activePrefix .. candidate) == 1 then
      redis.call('ZREM', queue, candidate)
      redis.call('HDEL', sockets, candidate)
    else
      local difference = math.abs(tonumber(candidates[i + 1]) - trophies)
      if not bestDifference or difference < bestDifference then
        bestId = candidate
        bestDifference = difference
      end
    end
  end
end
if not bestId then return {'WAITING'} end
local opponentSocket = redis.call('HGET', sockets, bestId)
redis.call('ZREM', queue, userId, bestId)
redis.call('HDEL', sockets, userId, bestId)
redis.call('SET', activePrefix .. userId, 'matching', 'EX', 30)
redis.call('SET', activePrefix .. bestId, 'matching', 'EX', 30)
return {'MATCH', bestId, opponentSocket}
`;

const REMOVE_QUEUE_SCRIPT = `
if redis.call('HGET', KEYS[2], ARGV[1]) == ARGV[3] then
  redis.call('ZREM', KEYS[1], ARGV[1])
  redis.call('HDEL', KEYS[2], ARGV[1])
end
return redis.call('GET', ARGV[2] .. ARGV[1])
`;

export function attachBattleSocket(io) {
  io.use(async (socket, next) => {
    const token = socket.handshake.auth?.token;
    if (typeof token !== 'string') return next(new Error('Authentication required.'));
    let payload;
    try {
      payload = await verifyAccessToken(token);
    } catch {
      next(new Error('Invalid or expired token.'));
      return;
    }
    try {
      const result = await query(
        `SELECT id, explorer_name, trophy_points FROM users
         WHERE id = $1 AND deleted_at IS NULL`,
        [payload.sub]
      );
      if (!result.rowCount) return next(new Error('Account unavailable.'));
      socket.data.user = result.rows[0];
      next();
    } catch (error) {
      console.error('Battle authentication lookup failed', error);
      next(new Error('Authentication service unavailable.'));
    }
  });

  io.on('connection', (socket) => {
    socket.on('find_match', async (_payload, acknowledge) => {
      try {
        const { id, trophy_points: trophies } = socket.data.user;
        const rateKey = `battle:find-rate:${id}`;
        const attempts = await redis.incr(rateKey);
        if (attempts === 1) await redis.expire(rateKey, 60);
        if (attempts > 10) {
          acknowledge?.({ ok: false, code: 'rate_limited', message: 'Please wait before searching again.' });
          return;
        }
        const result = await redis.eval(
          MATCH_SCRIPT,
          2,
          QUEUE_KEY,
          SOCKETS_KEY,
          'battle:user:',
          id,
          String(trophies),
          socket.id,
          String(env.MATCH_TROPHY_RANGE)
        );
        if (result[0] === 'ACTIVE') {
          acknowledge?.({ ok: false, code: 'battle_active', message: 'Finish your current battle first.' });
          return;
        }
        if (result[0] === 'WAITING') {
          socket.emit('match_waiting');
          acknowledge?.({ ok: true, status: 'waiting' });
          return;
        }

        const opponentId = result[1];
        const opponentSocketId = result[2];
        const opponentSockets = await io.in(opponentSocketId).fetchSockets();
        if (!opponentSockets.length) {
          await redis.del(`battle:user:${id}`, `battle:user:${opponentId}`);
          await redis.zadd(QUEUE_KEY, trophies, id);
          await redis.hset(SOCKETS_KEY, id, socket.id);
          socket.emit('match_waiting');
          acknowledge?.({ ok: true, status: 'waiting' });
          return;
        }
        const questionResult = await query(
          `SELECT id, prompt, answer, choices FROM vocabulary
           ORDER BY random() LIMIT 5`
        );
        if (questionResult.rowCount < 5) throw new Error('Insufficient battle questions configured.');
        const roomId = `battle:${randomUUID()}`;
        const state = {
          roomId,
          playerIds: [id, opponentId],
          names: {
            [id]: socket.data.user.explorer_name,
            [opponentId]: (await getUserName(opponentId))
          },
          socketIds: {
            [id]: socket.id,
            [opponentId]: opponentSocketId
          },
          scores: { [id]: 0, [opponentId]: 0 },
          questionIndex: 0,
          answers: {},
          questions: questionResult.rows,
          status: 'active'
        };
        await redis.set(`battle:state:${roomId}`, JSON.stringify(state), 'EX', 3600);
        await redis.set(`battle:user:${id}`, roomId, 'EX', 3600);
        await redis.set(`battle:user:${opponentId}`, roomId, 'EX', 3600);
        await Promise.all([
          socket.join(roomId),
          io.in(opponentSocketId).socketsJoin(roomId)
        ]);
        io.to(roomId).emit('match_found', {
          roomId,
          players: state.playerIds.map((playerId) => ({
            id: playerId,
            name: state.names[playerId]
          }))
        });
        io.to(roomId).emit('battle:question', publicQuestion(state, 0));
        acknowledge?.({ ok: true, status: 'matched', roomId });
      } catch (error) {
        console.error('Battle matchmaking failed', error);
        acknowledge?.({ ok: false, code: 'matchmaking_error', message: 'Could not find a match.' });
      }
    });

    socket.on('battle:answer', async (payload, acknowledge) => {
      const parsed = answerSchema.safeParse(payload);
      if (!parsed.success) {
        acknowledge?.({ ok: false, code: 'invalid_answer' });
        return;
      }
      try {
        const roomId = await redis.get(`battle:user:${socket.data.user.id}`);
        if (!roomId) {
          acknowledge?.({ ok: false, code: 'battle_not_found' });
          return;
        }
        const result = await answerQuestion(roomId, socket.data.user.id, parsed.data);
        if (result.error) {
          acknowledge?.({ ok: false, code: result.error });
          return;
        }
        acknowledge?.({ ok: true, correct: result.correct });
        if (!result.resolved) return;

        io.to(roomId).emit('battle:answer_result', {
          questionIndex: result.resolvedQuestionIndex,
          scores: result.state.scores
        });
        if (result.state.status === 'finished') {
          await persistBattleResult(result.state);
          await Promise.all(result.state.playerIds.map((playerId) => redis.del(`battle:user:${playerId}`)));
          io.to(roomId).emit('battle:finished', {
            scores: result.state.scores,
            winnerId: getWinner(result.state)
          });
        } else {
          io.to(roomId).emit('battle:question', publicQuestion(result.state, result.state.questionIndex));
        }
      } catch (error) {
        console.error('Battle answer processing failed', error);
        acknowledge?.({ ok: false, code: 'battle_error' });
      }
    });

    socket.on('battle:leave', async () => {
      await disconnectPlayer(io, socket);
    });

    socket.on('disconnect', async () => {
      await disconnectPlayer(io, socket);
    });
  });
}

async function getUserName(userId) {
  const result = await query('SELECT explorer_name FROM users WHERE id = $1', [userId]);
  return result.rows[0]?.explorer_name ?? 'Explorer';
}

async function answerQuestion(roomId, userId, answer) {
  const key = `battle:state:${roomId}`;
  for (let attempt = 0; attempt < 5; attempt += 1) {
    const lockKey = `${key}:lock`;
    const lockToken = randomUUID();
    if (await redis.set(lockKey, lockToken, 'PX', 5000, 'NX') !== 'OK') {
      await new Promise((resolve) => setTimeout(resolve, 25 + attempt * 25));
      continue;
    }
    try {
      const raw = await redis.get(key);
      if (!raw) return { error: 'battle_not_found' };
      const state = JSON.parse(raw);
      if (state.status !== 'active' || !state.playerIds.includes(userId)) {
        return { error: 'battle_not_active' };
      }
      if (answer.questionIndex !== state.questionIndex) return { error: 'stale_question' };
      if (Object.hasOwn(state.answers, userId)) return { error: 'answer_already_submitted' };
      const question = state.questions[state.questionIndex];
      const correct = question.answer.toLocaleLowerCase() === answer.answer.toLocaleLowerCase();
      state.answers[userId] = correct;
      const resolved = state.playerIds.every((playerId) => Object.hasOwn(state.answers, playerId));
      const resolvedQuestionIndex = state.questionIndex;
      if (resolved) {
        for (const playerId of state.playerIds) {
          if (state.answers[playerId]) state.scores[playerId] += 1;
        }
        state.questionIndex += 1;
        state.answers = {};
        if (state.questionIndex >= state.questions.length) state.status = 'finished';
      }
      await redis.set(key, JSON.stringify(state), 'EX', 3600);
      return { correct, resolved, resolvedQuestionIndex, state };
    } finally {
      await redis.eval(RELEASE_LOCK_SCRIPT, 1, lockKey, lockToken);
    }
  }
  return { error: 'battle_busy' };
}

async function disconnectPlayer(io, socket) {
  try {
    const userId = socket.data.user?.id;
    if (!userId) return;
    const roomId = await redis.eval(
      REMOVE_QUEUE_SCRIPT,
      2,
      QUEUE_KEY,
      SOCKETS_KEY,
      userId,
      'battle:user:',
      socket.id
    );
    if (!roomId) return;
    const key = `battle:state:${roomId}`;
    const raw = await redis.get(key);
    if (!raw) return;
    const state = await updateBattleOnDisconnect(roomId, userId, socket.id);
    if (!state) return;
    await redis.del(`battle:user:${userId}`);
    const opponentId = state.playerIds.find((playerId) => playerId !== userId);
    await redis.del(`battle:user:${opponentId}`);
    io.to(state.socketIds[opponentId]).emit('battle:opponent_disconnected');
    await socket.leave(roomId);
  } catch (error) {
    console.error('Battle disconnect cleanup failed', error);
  }
}

async function updateBattleOnDisconnect(roomId, userId, socketId) {
  const key = `battle:state:${roomId}`;
  for (let attempt = 0; attempt < 5; attempt += 1) {
    const lockKey = `${key}:lock`;
    const token = randomUUID();
    if (await redis.set(lockKey, token, 'PX', 5000, 'NX') !== 'OK') {
      await new Promise((resolve) => setTimeout(resolve, 25 + attempt * 25));
      continue;
    }
    try {
      const raw = await redis.get(key);
      if (!raw) return null;
      const state = JSON.parse(raw);
      if (state.status !== 'active' || !state.playerIds.includes(userId) || state.socketIds[userId] !== socketId) return null;
      state.status = 'abandoned';
      state.disconnectedPlayerId = userId;
      await redis.set(key, JSON.stringify(state), 'EX', 3600);
      return state;
    } finally {
      await redis.eval(RELEASE_LOCK_SCRIPT, 1, lockKey, token);
    }
  }
  return null;
}

async function persistBattleResult(state) {
  const winnerId = getWinner(state);
  if (!winnerId) return;
  const result = await query(
    `UPDATE users SET trophy_points = trophy_points + 10, updated_at = NOW()
     WHERE id = $1 RETURNING trophy_points`,
    [winnerId]
  );
  if (result.rowCount) {
    await redis.zadd('leaderboard:trophies', result.rows[0].trophy_points, winnerId);
  }
}

function getWinner(state) {
  const [first, second] = state.playerIds;
  if (state.scores[first] === state.scores[second]) return null;
  return state.scores[first] > state.scores[second] ? first : second;
}

function publicQuestion(state, index) {
  const question = state.questions[index];
  if (!question) return null;
  return {
    questionIndex: index,
    totalQuestions: state.questions.length,
    questionId: question.id,
    prompt: question.prompt,
    choices: question.choices
  };
}

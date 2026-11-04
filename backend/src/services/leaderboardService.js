import { query } from '../config/db.js';
import { redis } from '../config/redis.js';

const KEY = 'leaderboard:trophies';
const INITIALIZED_KEY = 'leaderboard:trophies:initialized';

export async function syncUserScore(userId, points) {
  await redis.zadd(KEY, points, userId);
}

export async function getLeaderboard(limit, userId) {
  const initialized = await redis.get(INITIALIZED_KEY);
  if (!initialized) {
    const initial = await query(
      `SELECT id, explorer_name, trophy_points
       FROM users WHERE deleted_at IS NULL ORDER BY id`
    );
    const pipeline = redis.pipeline();
    for (const user of initial.rows) pipeline.zadd(KEY, user.trophy_points, user.id);
    pipeline.set(INITIALIZED_KEY, '1');
    const results = await pipeline.exec();
    const failed = results?.find(([error]) => error);
    if (failed) throw failed[0];
  }
  const ids = await redis.zrevrange(KEY, 0, limit - 1);
  const userRank = userId ? await redis.zrevrank(KEY, userId) : null;
  const myScore = userId ? await redis.zscore(KEY, userId) : null;
  if (!ids.length) return { players: [], rank: userRank === null ? null : userRank + 1 };
  const players = await query(
    `SELECT id, explorer_name, trophy_points FROM users
     WHERE id = ANY($1::uuid[]) AND deleted_at IS NULL`,
    [ids]
  );
  const byId = new Map(players.rows.map((player) => [player.id, player]));
  return {
    players: ids.map((id, index) => {
      const player = byId.get(id);
      return player ? { ...player, rank: index + 1 } : null;
    }).filter(Boolean),
    rank: userRank === null ? null : userRank + 1,
    trophyPoints: myScore === null ? null : Number(myScore)
  };
}

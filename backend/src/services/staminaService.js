import { env } from '../config/env.js';

export async function refreshStamina(client, userId) {
  const result = await client.query(
    `UPDATE users
     SET stamina = LEAST(
           $2,
           stamina + FLOOR(EXTRACT(EPOCH FROM (NOW() - last_stamina_update)) / $3)::int
         ),
         last_stamina_update = CASE
           WHEN stamina >= $2 THEN NOW()
           ELSE last_stamina_update +
             (FLOOR(EXTRACT(EPOCH FROM (NOW() - last_stamina_update)) / $3)::int * $3) * INTERVAL '1 second'
         END,
         updated_at = NOW()
     WHERE id = $1
     RETURNING stamina, last_stamina_update`,
    [userId, env.STAMINA_MAX, env.STAMINA_REGEN_SECONDS]
  );
  return result.rows[0];
}

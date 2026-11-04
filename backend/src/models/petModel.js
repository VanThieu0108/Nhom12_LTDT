import { queryWith } from './queryWith.js';

export async function listPets(client, userId) {
  const result = await queryWith(client,
    `SELECT p.id, p.name, p.species, p.rarity, p.image_url,
            (up.id IS NOT NULL) AS unlocked, up.acquired_at
     FROM pets p
     LEFT JOIN user_pets up ON up.pet_id = p.id AND up.user_id = $1
     ORDER BY CASE p.rarity
       WHEN 'common' THEN 1 WHEN 'rare' THEN 2 WHEN 'epic' THEN 3 ELSE 4 END, p.name`,
    [userId]
  );
  return result.rows;
}

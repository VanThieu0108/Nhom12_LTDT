CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  explorer_name VARCHAR(32) NOT NULL,
  explorer_name_normalized VARCHAR(32) NOT NULL UNIQUE,
  pin_hash TEXT NOT NULL,
  coins INTEGER NOT NULL DEFAULT 150 CHECK (coins >= 0),
  stamina INTEGER NOT NULL DEFAULT 5 CHECK (stamina >= 0),
  last_stamina_update TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  experience INTEGER NOT NULL DEFAULT 0 CHECK (experience >= 0),
  trophy_points INTEGER NOT NULL DEFAULT 0 CHECK (trophy_points >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMPTZ
);

CREATE INDEX users_trophy_points_idx ON users (trophy_points DESC, id);

CREATE TABLE vocabulary (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  zone VARCHAR(16) NOT NULL CHECK (zone IN ('listening', 'speaking', 'reading', 'writing')),
  level INTEGER NOT NULL CHECK (level > 0),
  prompt TEXT NOT NULL,
  answer TEXT NOT NULL,
  translation TEXT NOT NULL,
  choices JSONB NOT NULL CHECK (jsonb_typeof(choices) = 'array'),
  audio_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX vocabulary_zone_level_idx ON vocabulary (zone, level);

CREATE TABLE user_progress (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  zone VARCHAR(16) NOT NULL,
  level INTEGER NOT NULL CHECK (level > 0),
  correct_answers INTEGER NOT NULL CHECK (correct_answers >= 0),
  total_questions INTEGER NOT NULL CHECK (total_questions > 0),
  points INTEGER NOT NULL CHECK (points >= 0),
  completed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, zone, level)
);

CREATE TABLE minigame_scores (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  game_type VARCHAR(24) NOT NULL CHECK (game_type IN ('word_match', 'runner', 'meteor')),
  score INTEGER NOT NULL CHECK (score >= 0),
  distance INTEGER NOT NULL DEFAULT 0 CHECK (distance >= 0),
  metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX minigame_scores_user_created_idx ON minigame_scores (user_id, created_at DESC);

CREATE TABLE pets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(48) NOT NULL UNIQUE,
  species VARCHAR(48) NOT NULL,
  rarity VARCHAR(16) NOT NULL CHECK (rarity IN ('common', 'rare', 'epic', 'legendary')),
  image_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_pets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  pet_id UUID NOT NULL REFERENCES pets(id),
  acquired_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  source VARCHAR(24) NOT NULL CHECK (source IN ('incubator', 'reward', 'admin')),
  UNIQUE (user_id, pet_id)
);

CREATE TABLE incubators (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  egg_type VARCHAR(16) NOT NULL CHECK (egg_type IN ('common', 'rare', 'epic')),
  start_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  hatch_duration_seconds INTEGER NOT NULL CHECK (hatch_duration_seconds > 0),
  claimed_at TIMESTAMPTZ,
  pet_id UUID REFERENCES pets(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX one_active_incubator_per_user
  ON incubators(user_id) WHERE claimed_at IS NULL;

CREATE INDEX incubators_user_created_idx ON incubators(user_id, created_at DESC);

INSERT INTO pets (name, species, rarity) VALUES
  ('Sunny Bunny', 'bunny', 'common'),
  ('Milo Fox', 'fox', 'common'),
  ('Cloud Cat', 'cat', 'common'),
  ('Golden Fox', 'fox', 'rare'),
  ('Moon Panda', 'panda', 'rare'),
  ('Star Dragon', 'dragon', 'epic'),
  ('Rainbow Unicorn', 'unicorn', 'legendary')
ON CONFLICT (name) DO NOTHING;

INSERT INTO vocabulary (zone, level, prompt, answer, translation, choices) VALUES
  ('reading', 1, '🍎', 'Apple', 'Quả táo', '["Quả táo", "Quả chuối", "Quả cam", "Quả nho"]'),
  ('reading', 1, '🐱', 'Cat', 'Con mèo', '["Con mèo", "Con chó", "Con chim", "Con cá"]'),
  ('reading', 1, '☀️', 'Sun', 'Mặt trời', '["Mặt trời", "Mặt trăng", "Ngôi sao", "Đám mây"]'),
  ('reading', 1, '🐶', 'Dog', 'Con chó', '["Con chó", "Con mèo", "Con vịt", "Con cáo"]'),
  ('writing', 1, 'Quả táo', 'Apple', 'Quả táo', '["Apple", "Banana", "Orange", "Grape"]'),
  ('writing', 1, 'Con mèo', 'Cat', 'Con mèo', '["Cat", "Dog", "Bird", "Fish"]'),
  ('writing', 1, 'Mặt trời', 'Sun', 'Mặt trời', '["Sun", "Moon", "Star", "Cloud"]'),
  ('writing', 1, 'Con chó', 'Dog', 'Con chó', '["Dog", "Cat", "Duck", "Fox"]'),
  ('listening', 1, 'What is red and grows on a tree?', 'Apple', 'Quả táo', '["Apple", "Banana", "Orange", "Grape"]'),
  ('listening', 1, 'Which pet says meow?', 'Cat', 'Con mèo', '["Cat", "Dog", "Bird", "Fish"]'),
  ('speaking', 1, 'Say the word: 🍎', 'Apple', 'Quả táo', '["Apple", "Banana", "Orange", "Grape"]'),
  ('speaking', 1, 'Say the word: 🐱', 'Cat', 'Con mèo', '["Cat", "Dog", "Bird", "Fish"]');

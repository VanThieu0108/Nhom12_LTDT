ALTER TABLE vocabulary
  ADD COLUMN category VARCHAR(50) NOT NULL DEFAULT 'General',
  ADD COLUMN phonetic VARCHAR(100),
  ADD COLUMN image_url TEXT;

UPDATE vocabulary
SET category = CASE
  WHEN lower(answer) IN ('apple', 'banana', 'orange', 'grape') THEN 'Fruits'
  WHEN lower(answer) IN ('cat', 'dog', 'bird', 'fish', 'duck', 'fox') THEN 'Animals'
  WHEN lower(answer) IN ('sun', 'moon', 'star', 'cloud') THEN 'Nature'
  ELSE 'General'
END;

CREATE INDEX vocabulary_level_category_idx ON vocabulary (level, category);

CREATE TABLE vocabulary_words (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  word_en VARCHAR(100) NOT NULL,
  meaning_vi VARCHAR(100) NOT NULL,
  phonetic VARCHAR(100),
  audio_url TEXT,
  image_url TEXT,
  difficulty_level INTEGER NOT NULL CHECK (difficulty_level BETWEEN 1 AND 100),
  category VARCHAR(50) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX vocabulary_words_unique_idx
  ON vocabulary_words (difficulty_level, category, lower(word_en), lower(meaning_vi));
CREATE INDEX vocabulary_words_level_category_idx
  ON vocabulary_words (difficulty_level, category);

INSERT INTO vocabulary_words (
  word_en, meaning_vi, difficulty_level, category, phonetic, audio_url, image_url
)
SELECT DISTINCT ON (level, category, lower(answer), lower(translation))
  answer, translation, level, category, phonetic, audio_url, image_url
FROM vocabulary
ORDER BY level, category, lower(answer), lower(translation), created_at;

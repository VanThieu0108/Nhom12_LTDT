ALTER TABLE user_progress
  ADD COLUMN passed BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE user_progress
SET passed = correct_answers * 5 >= total_questions * 4;

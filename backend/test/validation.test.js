import { before, test } from 'node:test';
import assert from 'node:assert/strict';

process.env.NODE_ENV = 'test';
process.env.DATABASE_URL = 'postgresql://test:test@localhost:5432/test';
process.env.REDIS_URL = 'redis://127.0.0.1:6379';
process.env.JWT_SECRET = 'test-only-secret-with-at-least-32-characters';

let registerSchema;
let loginSchema;
let scoreSchema;
let completeLevelSchema;
let incubateSchema;

before(async () => {
  ({ registerSchema, loginSchema } = await import('../src/controllers/authController.js'));
  ({ scoreSchema } = await import('../src/controllers/minigameController.js'));
  ({ completeLevelSchema } = await import('../src/controllers/studyController.js'));
  ({ incubateSchema } = await import('../src/controllers/petController.js'));
});

const validQuestionId = '93e1af55-837a-4f20-8ea9-152d0dd5a990';

test('registration accepts a bounded explorer name and numeric PIN', () => {
  assert.equal(registerSchema.safeParse({ explorerName: 'Mia Explorer', pin: '123456' }).success, true);
  assert.equal(registerSchema.safeParse({ explorerName: 'Mia', pin: '12ab' }).success, false);
  assert.equal(registerSchema.safeParse({ explorerName: 'x', pin: '1234' }).success, false);
});

test('login rejects malformed PINs before database work', () => {
  assert.equal(loginSchema.safeParse({ explorerName: 'Mia', pin: '1234' }).success, true);
  assert.equal(loginSchema.safeParse({ explorerName: 'Mia', pin: 'abcdef' }).success, false);
});

test('minigame score validation bounds client-provided values', () => {
  assert.equal(scoreSchema.safeParse({ gameType: 'runner', score: 500, distance: 100 }).success, true);
  assert.equal(scoreSchema.safeParse({ gameType: 'battle', score: 5 }).success, false);
  assert.equal(scoreSchema.safeParse({ gameType: 'runner', score: 100001 }).success, false);
});

test('level submissions require unique-looking question UUID format and answers', () => {
  assert.equal(completeLevelSchema.safeParse({
    answers: [{ questionId: validQuestionId, answer: 'Apple' }]
  }).success, true);
  assert.equal(completeLevelSchema.safeParse({
    answers: [{ questionId: 'not-a-uuid', answer: 'Apple' }]
  }).success, false);
  assert.equal(completeLevelSchema.safeParse({ answers: [] }).success, false);
});

test('incubation only accepts supported egg types', () => {
  assert.equal(incubateSchema.safeParse({ eggType: 'rare' }).success, true);
  assert.equal(incubateSchema.safeParse({ eggType: 'mythic' }).success, false);
  assert.equal(incubateSchema.safeParse({}).data.eggType, 'common');
});

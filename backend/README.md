# Kids English Adventure backend

Node.js 20+, PostgreSQL 13+, Express, Redis and Socket.IO API. This folder is independent from the Android Gradle project.

## Run locally

1. Copy `.env.example` to `.env` and set a unique `JWT_SECRET` (at least 32 characters).
2. Run `docker compose up -d` to start local PostgreSQL and Redis, then run `npm install`.
3. Run `npm run migrate` to create the schema and seed sample vocabulary and pets.
4. Run `npm run dev`. `GET /health` checks PostgreSQL and Redis.

The Compose defaults are for local development only and bind databases to loopback.

For production, use TLS for PostgreSQL/Redis, a managed secret store, a restrictive CORS allow-list, and set `TRUST_PROXY` to the exact trusted proxy hop count/address when behind a reverse proxy. PINs are hashed with bcrypt; login and registration are rate-limited. Never ship service credentials or the JWT secret in the Android app.

## Authentication

`POST /api/auth/register` accepts `{ "explorerName": "Mia", "pin": "123456" }`.
`POST /api/auth/login` returns a short-lived bearer JWT. Send it as
`Authorization: Bearer <token>` for protected REST endpoints, or as the Socket.IO
`auth.token` value during the socket handshake.

## API

- `GET /health`
- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`
- `GET /api/study/zones`, `GET /api/study/zones/:zone/levels/:level/questions`
- `POST /api/study/zones/:zone/levels/:level/complete`
- `POST /api/minigames/scores`, `GET /api/minigames/scores`
- `GET /api/pets`, `POST /api/pets/incubators`, `POST /api/pets/incubators/:id/claim`
- `GET /api/leaderboard?limit=20`
- `GET /api/v1/vocabulary/levels`
- `GET /api/v1/vocabulary/categories?level=2`
- `GET /api/v1/vocabulary?level=2&category=Animals&limit=10`
- `GET /api/v1/vocabulary?limit=8&order=difficulty&offset=0`
- `GET /api/v1/vocabulary?limit=10&order=listening&offset=0`
- `GET /api/v1/vocabulary?limit=10&order=speaking&offset=0`
- `GET /api/v1/vocabulary?limit=10&order=reading&offset=0`
- `GET /api/v1/vocabulary?limit=10&order=writing&offset=0`

Vocabulary endpoints are read-only and public so the mobile app can preload
learning content before a learner signs in. A CSV bulk import uses
`npm run import:vocabulary -- --file .\data\vocabulary.csv`. Required CSV headers
are `word_en,meaning_vi,difficulty_level,category`; optional headers are
`phonetic,audio_url,image_url`. The importer validates every row before writing,
uses one transaction, skips existing duplicates, and reports inserted/skipped
counts. Example:

```csv
word_en,meaning_vi,difficulty_level,category,phonetic,audio_url,image_url
Apple,Quả táo,1,Fruits,/ˈæp.əl/,https://example.com/apple.mp3,https://example.com/apple.png
Dog,Con chó,1,Animals,/dɒɡ/,,
```

The Android debug build connects to the development backend through the
emulator's `10.0.2.2` host alias. For a physical phone, build with
`-PVOCABULARY_API_BASE_URL=http://<computer-LAN-IP>:3000` while both devices are
on the same trusted network. Release builds should use an HTTPS API URL.

`word_en` and `meaning_vi` are stored as vocabulary dictionary entries, separate
from quiz questions in `vocabulary`. Quiz-question levels also include category,
phonetic, and image metadata, so existing lesson routes keep working.
The Word Match trail requests eight unique words per stage using the global
difficulty order (dictionary tier, then word length), with an offset of
`(stage - 1) * 8`.
Listening uses the same global tier/length difficulty order and requests ten
unique words or phrases per lesson with `offset=(level - 1) * 20`. The 20-row
stride spreads 50 lessons across the dictionary's three difficulty tiers while
keeping each lesson's ten answers unique. Its ordering accepts English words
and space/hyphen-separated phrases up to 60 characters.
Speaking uses the same 20-row difficulty stride and requests ten unique prompts
per lesson. Levels 1–30 practice the vocabulary directly, levels 31–40 wrap
items in short spoken phrases, and levels 41–50 use longer sentence prompts.
Reading uses the same 20-row stride and ten unique words or phrases per lesson.
The Android lesson adds progressively longer context from single-word reading
at levels 1–15 to phrases at 16–30, sentence context at 31–40, and short
paragraph context at 41–50. Reading completion is tracked independently on the
device; learners need at least 8 correct answers out of 10 to unlock the next
level.
Writing uses the same 20-row stride and ten unique spelling prompts per lesson.
The ordered vocabulary progresses from shorter words toward longer and more
complex entries; levels 41–50 practice a short sentence assembled from each
entry. Writing progress is stored separately on the device, and 8 correct
answers out of 10 unlock the next level.

### Word Match 50-level trail

The Android Match level map is opened from the arcade or the home-screen Match
shortcut. Level 1 is initially available; completing its eight pairs unlocks
Level 2, continuing sequentially through Level 50. Completion state is stored
on the device with Android `SharedPreferences`, so it survives app restarts but
is not synchronized between devices or accounts. No database migration is
needed for this local progression.

Restart the backend after updating its source, then verify the first and last
stage with `GET /api/v1/vocabulary?limit=8&order=difficulty&offset=0` and
`GET /api/v1/vocabulary?limit=8&order=difficulty&offset=392`. Build and install
the updated Android app; every stage uses an offset of `(level - 1) * 8`.

Study levels unlock in order within each learning zone. Level 1 is open by
default; the learner must answer at least 80% correctly to pass and unlock the
next level. Failed attempts can be retried, but do not grant gold or trophy
points. The study-zone response includes an `unlocked` flag for each available
zone/level, and progress entries include `passed`.

## Admin website

Open `http://localhost:3000/admin` to manage the app. Configure `ADMIN_USERNAME`
and a unique `ADMIN_PASSWORD` (at least 16 characters) in `.env` before signing
in. The admin session is a short-lived bearer token stored for the current browser
tab. The dashboard shows app usage and lets administrators view players and
imported dictionary entries, create/edit/delete quiz questions and pets. A pet
cannot be deleted while it is in a player's collection.

Level completion body: `{ "answers": [{ "questionId": "uuid", "answer": "Apple" }] }`.
Minigame score body: `{ "gameType": "word_match", "score": 80, "distance": 0, "metadata": {} }`.
Egg incubation body: `{ "eggType": "common" }`. Hatch time is checked against the
database clock and cannot be shortened by a client.

Minigame submissions are stored as untrusted client-reported scores. They do not
grant coins or trophy points; only server-graded study completion and battle
results affect the rewards leaderboard. To award minigame prizes, add server-side
game sessions/event verification rather than trusting a final score from the app.

## Battles

Socket events: `find_match`, `battle:answer`, `battle:leave`. Server responses:
`match_found`, `battle:question`, `battle:answer_result`, `battle:finished`,
`battle:opponent_disconnected`, `match_waiting`, and `battle:error`.
Answers are graded on the server. Redis atomically reserves matchmaking entries
and stores battle state; the Socket.IO Redis adapter distributes private-room
events between backend instances.

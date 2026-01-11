
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  username TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  creation_time TIMESTAMP NOT NULL,
  last_upd_time TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS profiles (
  id UUID PRIMARY KEY,
  user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  creation_time TIMESTAMP NOT NULL,
  last_upd_time TIMESTAMP NOT NULL,
  total_ratings INT NOT NULL DEFAULT 0,
  average_score DOUBLE PRECISION NOT NULL DEFAULT 0,
  favourite_genre TEXT,
  bio TEXT,
  avatar_url TEXT
);

CREATE TABLE IF NOT EXISTS media (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  description TEXT,
  media_type TEXT,
  release_year INT NOT NULL,
  genres TEXT[],                     -- save genre name as array
  age_restriction INT NOT NULL,
  creation_time TIMESTAMP NOT NULL,
  last_upd_time TIMESTAMP NOT NULL,
  total_likes INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ratings (
  id UUID PRIMARY KEY,
  media_id UUID NOT NULL REFERENCES media(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  stars INT NOT NULL CHECK (stars >= 1 AND stars <= 5),
  comment TEXT,
  status TEXT NOT NULL,              -- PENDING/CONFIRMED/REJECTED
  creation_time TIMESTAMP NOT NULL,
  last_upd_time TIMESTAMP NOT NULL,
  total_likes INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS favorites (
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  media_id UUID NOT NULL REFERENCES media(id) ON DELETE CASCADE,
  creation_time TIMESTAMP NOT NULL DEFAULT NOW(),
  PRIMARY KEY (user_id, media_id)
);

CREATE INDEX IF NOT EXISTS idx_media_user_id ON media(user_id);
CREATE INDEX IF NOT EXISTS idx_ratings_media_id ON ratings(media_id);
CREATE INDEX IF NOT EXISTS idx_ratings_user_id ON ratings(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);

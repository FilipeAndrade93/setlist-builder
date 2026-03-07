CREATE TYPE song_source AS ENUM ('spotify', 'manual', 'arrangement');

CREATE TABLE songs (
                       id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name             VARCHAR(255) NOT NULL,
                       duration_seconds INT NOT NULL CHECK (duration_seconds > 0),
                       spotify_id       VARCHAR(255) UNIQUE,
                       source           song_source NOT NULL DEFAULT 'manual',
                       original_song_id UUID REFERENCES songs(id) ON DELETE SET NULL,
                       deleted_at       TIMESTAMP DEFAULT NULL,
                       created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE setlists (
                          id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          venue_name  VARCHAR(255) NOT NULL,
                          event_date  DATE NOT NULL,
                          created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                          CONSTRAINT uq_setlist_venue_date UNIQUE (venue_name, event_date)
);

CREATE TABLE setlist_songs (
                               setlist_id  UUID NOT NULL REFERENCES setlists(id) ON DELETE CASCADE,
                               song_id     UUID NOT NULL REFERENCES songs(id),
                               position    INT NOT NULL,
                               PRIMARY KEY (setlist_id, song_id)
);

CREATE TABLE app_users (
                           id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           username      VARCHAR(100) NOT NULL UNIQUE,
                           password_hash VARCHAR(255) NOT NULL,
                           created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
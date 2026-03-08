ALTER TABLE songs ADD COLUMN popularity INT DEFAULT 0 CHECK (popularity >= 0 AND popularity <= 100);
ALTER TABLE songs ADD COLUMN popularity_synced_at TIMESTAMP DEFAULT NULL;
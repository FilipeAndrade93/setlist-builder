ALTER TABLE songs DROP CONSTRAINT songs_duration_seconds_check;
ALTER TABLE songs ADD CONSTRAINT songs_duration_seconds_check CHECK (duration_seconds >= 0);
ALTER TABLE app_users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'MEMBER';

UPDATE app_users SET role = 'ADMIN' WHERE username = 'bombazine';
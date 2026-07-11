ALTER TABLE users ADD COLUMN google_access_token VARCHAR(512);
ALTER TABLE users ADD COLUMN google_refresh_token VARCHAR(512);
ALTER TABLE users ADD COLUMN google_token_expires_at DATETIME(6);
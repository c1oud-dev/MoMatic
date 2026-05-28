ALTER TABLE meetings
    ADD COLUMN stored_file_name VARCHAR(255) NOT NULL DEFAULT 'unknown',
    ADD COLUMN original_file_name VARCHAR(255) NOT NULL DEFAULT 'unknown',
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PENDING';
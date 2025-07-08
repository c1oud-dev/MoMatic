-- H2 dev
ALTER TABLE action_item ALTER COLUMN due_date VARCHAR(255);

-- MySQL prod (적용 시)
-- ALTER TABLE action_item MODIFY due_date VARCHAR(255);
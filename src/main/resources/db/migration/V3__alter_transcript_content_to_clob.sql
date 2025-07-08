ALTER TABLE transcript ALTER COLUMN content CLOB;

-- MySQL prod (적용 시)
-- ALTER TABLE transcript MODIFY content LONGTEXT;
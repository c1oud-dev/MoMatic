ALTER TABLE meeting ALTER COLUMN summary CLOB;

-- MySQL prod (적용 시)
-- ALTER TABLE meeting MODIFY summary LONGTEXT;
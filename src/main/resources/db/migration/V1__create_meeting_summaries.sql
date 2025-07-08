CREATE TABLE meeting_summaries (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id  VARCHAR(100) NOT NULL,
    summary_text LONGTEXT     NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_meeting (meeting_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

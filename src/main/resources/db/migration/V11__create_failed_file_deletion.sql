CREATE TABLE failed_file_deletion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stored_file_name VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    last_attempt_at TIMESTAMP NULL
);
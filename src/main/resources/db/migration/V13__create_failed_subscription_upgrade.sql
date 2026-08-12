CREATE TABLE failed_subscription_upgrade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    last_attempt_at TIMESTAMP NULL,
    last_error_message VARCHAR(500) NULL
);

CREATE INDEX idx_failed_subscription_upgrade_status
    ON failed_subscription_upgrade (status);
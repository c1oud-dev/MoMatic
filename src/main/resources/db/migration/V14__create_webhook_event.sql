CREATE TABLE webhook_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transmission_id VARCHAR(100) NOT NULL UNIQUE,
    event_type VARCHAR(50) NULL,
    order_id VARCHAR(100) NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    retried_count INT NOT NULL DEFAULT 0,
    last_error_message VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP NULL
);

CREATE INDEX idx_webhook_event_status
    ON webhook_event (status);
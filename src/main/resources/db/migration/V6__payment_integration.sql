CREATE TABLE payments_new (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    payment_key VARCHAR(255),
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    plan_type VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_payments_new_order_id UNIQUE (order_id),
    CONSTRAINT uk_payments_new_payment_key UNIQUE (payment_key),
    CONSTRAINT fk_payments_new_user FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO payments_new (
    id,
    order_id,
    payment_key,
    amount,
    status,
    plan_type,
    user_id,
    created_at,
    updated_at
)
SELECT
    payments.id,
    CONCAT('legacy-', payments.id),
    payments.toss_payment_key,
    COALESCE(payments.amount, 0),
    COALESCE(payments.status, 'FAILED'),
    subscriptions.plan_type,
    subscriptions.user_id,
    payments.created_at,
    payments.updated_at
FROM payments
INNER JOIN subscriptions
    ON subscriptions.id = payments.subscription_id;

DROP TABLE payments;

ALTER TABLE payments_new
    RENAME TO payments;

CREATE INDEX idx_payments_user_id ON payments(user_id);
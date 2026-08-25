UPDATE users
SET role = CONCAT('ROLE_', role)
WHERE role NOT LIKE 'ROLE!_%' ESCAPE '!';

ALTER TABLE payments
    MODIFY COLUMN status VARCHAR(30) NOT NULL;

CREATE INDEX idx_payments_status_created_at
    ON payments(status, created_at);
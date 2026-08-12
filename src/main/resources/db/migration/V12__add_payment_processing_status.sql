CREATE INDEX idx_payments_status_updated_at
    ON payments (status, updated_at);
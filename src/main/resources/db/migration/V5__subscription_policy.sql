ALTER TABLE subscriptions
    RENAME COLUMN started_date TO started_at;

ALTER TABLE subscriptions
    RENAME COLUMN ended_date TO expired_at;

ALTER TABLE subscriptions
    MODIFY COLUMN started_at TIMESTAMP NULL;

ALTER TABLE subscriptions
    MODIFY COLUMN expired_at TIMESTAMP NULL;
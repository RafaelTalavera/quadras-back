CREATE TABLE schedule_locks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lock_type VARCHAR(40) NOT NULL,
    scope_key VARCHAR(120) NOT NULL,
    booking_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_schedule_locks_scope UNIQUE (lock_type, scope_key, booking_date)
);

CREATE INDEX idx_schedule_locks_lookup
    ON schedule_locks (lock_type, scope_key, booking_date);

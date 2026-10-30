CREATE TABLE maintenance_notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    recipient_username VARCHAR(120) NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    schedule_version BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    due_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP NULL,
    resolved_at TIMESTAMP NULL,
    dismissed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_maintenance_notifications_order
        FOREIGN KEY (order_id) REFERENCES maintenance_orders (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_maintenance_notifications_dedup
        UNIQUE (order_id, notification_type, schedule_version, recipient_username)
);

CREATE INDEX idx_maintenance_notifications_recipient_status
    ON maintenance_notifications (recipient_username, status, created_at);
CREATE INDEX idx_maintenance_notifications_due
    ON maintenance_notifications (status, due_at);

CREATE TABLE notification_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    notification_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    logical_destination VARCHAR(160) NOT NULL,
    delivery_status VARCHAR(20) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NULL,
    last_error VARCHAR(500) NULL,
    idempotency_key VARCHAR(220) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP NULL,
    abandoned_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_outbox_notification
        FOREIGN KEY (notification_id) REFERENCES maintenance_notifications (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_notification_outbox_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX idx_notification_outbox_delivery
    ON notification_outbox (delivery_status, next_attempt_at, created_at);

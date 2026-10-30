ALTER TABLE notification_outbox
    ADD COLUMN claimed_at TIMESTAMP NULL;

CREATE INDEX idx_notification_outbox_claim
    ON notification_outbox (delivery_status, claimed_at);

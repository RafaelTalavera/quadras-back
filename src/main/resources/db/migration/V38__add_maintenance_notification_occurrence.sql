CREATE INDEX idx_maintenance_notifications_order
    ON maintenance_notifications (order_id);

ALTER TABLE maintenance_notifications
    DROP CONSTRAINT uk_maintenance_notifications_dedup;

ALTER TABLE maintenance_notifications
    ADD COLUMN occurrence_key VARCHAR(80) NOT NULL DEFAULT 'INITIAL';

ALTER TABLE maintenance_notifications
    ADD CONSTRAINT uk_maintenance_notifications_occurrence
        UNIQUE (order_id, notification_type, schedule_version, recipient_username, occurrence_key);

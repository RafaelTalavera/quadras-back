ALTER TABLE massage_bookings
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED';

ALTER TABLE massage_bookings
    ADD COLUMN cancellation_notes VARCHAR(500);

ALTER TABLE massage_bookings
    ADD COLUMN cancelled_at TIMESTAMP NULL;

ALTER TABLE massage_bookings
    ADD COLUMN created_by VARCHAR(80) NOT NULL DEFAULT 'system.migration';

ALTER TABLE massage_bookings
    ADD COLUMN updated_by VARCHAR(80) NOT NULL DEFAULT 'system.migration';

ALTER TABLE massage_bookings
    ADD COLUMN cancelled_by VARCHAR(80);

ALTER TABLE massage_bookings
    DROP INDEX uk_massage_bookings_provider_slot;

CREATE INDEX idx_massage_bookings_provider_slot
    ON massage_bookings (provider_id, booking_date, start_time);

CREATE INDEX idx_massage_bookings_status
    ON massage_bookings (status, booking_date, provider_id);

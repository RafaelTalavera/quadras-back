ALTER TABLE court_bookings
    ADD COLUMN recurrence_group_id VARCHAR(36) NULL;

ALTER TABLE court_bookings
    ADD COLUMN recurrence_start_date DATE NULL;

ALTER TABLE court_bookings
    ADD COLUMN recurrence_end_date DATE NULL;

CREATE INDEX idx_court_bookings_recurrence_group
    ON court_bookings (recurrence_group_id, booking_date, status);

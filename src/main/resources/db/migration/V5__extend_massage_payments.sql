ALTER TABLE massage_bookings
    ADD COLUMN payment_method VARCHAR(20);

ALTER TABLE massage_bookings
    ADD COLUMN payment_date DATE;

ALTER TABLE massage_bookings
    ADD COLUMN payment_notes VARCHAR(500);

CREATE INDEX idx_massage_bookings_paid
    ON massage_bookings (paid, booking_date, provider_id);

ALTER TABLE court_bookings
    ADD COLUMN partner_coach_id BIGINT NULL;

UPDATE court_bookings booking
SET partner_coach_id = (
    SELECT coach.id
    FROM court_partner_coaches coach
    WHERE LOWER(coach.name) = LOWER(booking.customer_name)
)
WHERE booking.customer_type = 'PARTNER_COACH'
  AND EXISTS (
    SELECT 1
    FROM court_partner_coaches coach
    WHERE LOWER(coach.name) = LOWER(booking.customer_name)
  );

ALTER TABLE court_bookings
    ADD CONSTRAINT fk_court_bookings_partner_coach
        FOREIGN KEY (partner_coach_id) REFERENCES court_partner_coaches(id);

CREATE INDEX idx_court_bookings_partner_coach_schedule
    ON court_bookings (partner_coach_id, booking_date, start_time, id);

ALTER TABLE audit_events
    ADD COLUMN actor_provider_type VARCHAR(30) NULL;

ALTER TABLE audit_events
    ADD COLUMN actor_provider_resource_id BIGINT NULL;

CREATE INDEX idx_audit_events_provider_actor
    ON audit_events (actor_provider_type, actor_provider_resource_id, occurred_at, id);

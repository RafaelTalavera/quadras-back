CREATE TABLE massage_therapists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_massage_therapists_provider
        FOREIGN KEY (provider_id) REFERENCES massage_providers (id),
    CONSTRAINT uk_massage_therapists_provider_name UNIQUE (provider_id, name)
);

ALTER TABLE massage_bookings
    ADD COLUMN therapist_id BIGINT NULL;

INSERT INTO massage_therapists (provider_id, name, active, created_at, updated_at)
SELECT mp.id, mp.name, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM massage_providers mp;

UPDATE massage_bookings mb
SET therapist_id = (
    SELECT mt.id
    FROM massage_therapists mt
    WHERE mt.provider_id = mb.provider_id
    ORDER BY mt.id
    LIMIT 1
)
WHERE mb.therapist_id IS NULL;

ALTER TABLE massage_bookings
    MODIFY COLUMN therapist_id BIGINT NOT NULL;

ALTER TABLE massage_bookings
    ADD CONSTRAINT fk_massage_bookings_therapist
        FOREIGN KEY (therapist_id) REFERENCES massage_therapists (id);

CREATE INDEX idx_massage_bookings_therapist_slot
    ON massage_bookings (therapist_id, booking_date, start_time, status);

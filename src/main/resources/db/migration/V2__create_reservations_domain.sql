CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guest_name VARCHAR(120) NOT NULL,
    reservation_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_reservations_time_window CHECK (start_time < end_time),
    CONSTRAINT chk_reservations_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_reservations_date_time
    ON reservations (reservation_date, start_time, end_time);

INSERT INTO app_metadata (property_key, property_value)
VALUES ('domain.reservations', 'hito4')
ON DUPLICATE KEY UPDATE property_value = VALUES(property_value);

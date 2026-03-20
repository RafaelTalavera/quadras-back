CREATE TABLE IF NOT EXISTS massage_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    specialty VARCHAR(120) NOT NULL,
    contact VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_massage_providers_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS massage_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    client_name VARCHAR(120) NOT NULL,
    guest_reference VARCHAR(120) NOT NULL,
    treatment VARCHAR(120) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    provider_id BIGINT NOT NULL,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_massage_bookings_provider
        FOREIGN KEY (provider_id) REFERENCES massage_providers (id),
    CONSTRAINT uk_massage_bookings_provider_slot UNIQUE (provider_id, booking_date, start_time),
    CONSTRAINT chk_massage_bookings_amount CHECK (amount > 0)
);

CREATE INDEX idx_massage_bookings_date_time
    ON massage_bookings (booking_date, start_time);

INSERT INTO app_metadata (property_key, property_value)
VALUES ('domain.massages', 'post-hito12')
ON DUPLICATE KEY UPDATE property_value = VALUES(property_value);

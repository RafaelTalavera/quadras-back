CREATE TABLE tour_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    contact VARCHAR(160) NOT NULL,
    default_commission_percent DECIMAL(5, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(120) NULL,
    CONSTRAINT uk_tour_providers_name_contact UNIQUE (name, contact),
    CONSTRAINT chk_tour_providers_commission_percent
        CHECK (default_commission_percent >= 0 AND default_commission_percent <= 100)
);

CREATE TABLE tour_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_type VARCHAR(20) NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    client_name VARCHAR(120) NOT NULL,
    guest_reference VARCHAR(120) NOT NULL,
    provider_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    commission_percent DECIMAL(5, 2) NOT NULL,
    commission_amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(1000) NULL,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    payment_method VARCHAR(30) NULL,
    payment_date DATE NULL,
    payment_notes VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL,
    cancellation_notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    cancelled_by VARCHAR(120) NULL,
    CONSTRAINT fk_tour_bookings_provider
        FOREIGN KEY (provider_id) REFERENCES tour_providers (id),
    CONSTRAINT chk_tour_bookings_amount CHECK (amount >= 0),
    CONSTRAINT chk_tour_bookings_commission_percent
        CHECK (commission_percent >= 0 AND commission_percent <= 100),
    CONSTRAINT chk_tour_bookings_commission_amount CHECK (commission_amount >= 0),
    CONSTRAINT chk_tour_bookings_window CHECK (end_at > start_at)
);

CREATE INDEX idx_tour_bookings_start_at
    ON tour_bookings (start_at, status);

CREATE INDEX idx_tour_bookings_provider_start_at
    ON tour_bookings (provider_id, start_at, status);

CREATE INDEX idx_tour_bookings_paid
    ON tour_bookings (paid, start_at, provider_id);

INSERT INTO tour_providers (name, contact, default_commission_percent, active, updated_by)
VALUES
    ('Agencia Costa Norte', 'concierge@costanorte.local', 10.00, TRUE, 'system'),
    ('Traslados Ilha Sul', '+55 48 99999-1000', 12.50, TRUE, 'system');

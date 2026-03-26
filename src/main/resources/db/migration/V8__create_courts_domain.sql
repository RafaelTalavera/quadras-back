CREATE TABLE court_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_type VARCHAR(40) NOT NULL,
    pricing_period VARCHAR(20) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(120) NULL,
    CONSTRAINT uk_court_rates_type_period UNIQUE (customer_type, pricing_period)
);

CREATE TABLE court_material_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    label VARCHAR(120) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    charge_guest BOOLEAN NOT NULL DEFAULT FALSE,
    charge_vip BOOLEAN NOT NULL DEFAULT FALSE,
    charge_external BOOLEAN NOT NULL DEFAULT TRUE,
    charge_partner_coach BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(120) NULL,
    CONSTRAINT uk_court_material_settings_code UNIQUE (code)
);

CREATE TABLE court_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_minutes INT NOT NULL,
    customer_name VARCHAR(120) NOT NULL,
    customer_reference VARCHAR(120) NOT NULL,
    customer_type VARCHAR(40) NOT NULL,
    pricing_period VARCHAR(20) NOT NULL,
    sunrise_estimate TIME NOT NULL,
    sunset_estimate TIME NOT NULL,
    court_amount DECIMAL(10, 2) NOT NULL,
    materials_amount DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
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
    cancelled_by VARCHAR(120) NULL
);

CREATE TABLE court_booking_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    court_booking_id BIGINT NOT NULL,
    material_code VARCHAR(30) NOT NULL,
    material_label VARCHAR(120) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_court_booking_materials_booking
        FOREIGN KEY (court_booking_id) REFERENCES court_bookings (id)
);

CREATE INDEX idx_court_bookings_slot
    ON court_bookings (booking_date, start_time, end_time, status);

CREATE INDEX idx_court_bookings_customer_type
    ON court_bookings (customer_type, booking_date, status);

INSERT INTO court_rates (customer_type, pricing_period, amount, active, updated_by)
VALUES
    ('GUEST', 'DAY', 0.00, TRUE, 'system'),
    ('GUEST', 'NIGHT', 0.00, TRUE, 'system'),
    ('VIP', 'DAY', 0.00, TRUE, 'system'),
    ('VIP', 'NIGHT', 0.00, TRUE, 'system'),
    ('EXTERNAL', 'DAY', 60.00, TRUE, 'system'),
    ('EXTERNAL', 'NIGHT', 80.00, TRUE, 'system'),
    ('PARTNER_COACH', 'DAY', 0.00, TRUE, 'system'),
    ('PARTNER_COACH', 'NIGHT', 0.00, TRUE, 'system');

INSERT INTO court_material_settings (
    code,
    label,
    unit_price,
    charge_guest,
    charge_vip,
    charge_external,
    charge_partner_coach,
    active,
    updated_by
)
VALUES
    ('RACKET', 'Raqueta', 20.00, FALSE, FALSE, TRUE, FALSE, TRUE, 'system'),
    ('BALL', 'Pelota', 10.00, FALSE, FALSE, TRUE, FALSE, TRUE, 'system');

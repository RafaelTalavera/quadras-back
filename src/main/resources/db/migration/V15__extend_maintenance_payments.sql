ALTER TABLE maintenance_orders
    ADD COLUMN paid BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE maintenance_orders
    ADD COLUMN payment_method VARCHAR(30) NULL;

ALTER TABLE maintenance_orders
    ADD COLUMN payment_date DATE NULL;

ALTER TABLE maintenance_orders
    ADD COLUMN payment_notes VARCHAR(500) NULL;

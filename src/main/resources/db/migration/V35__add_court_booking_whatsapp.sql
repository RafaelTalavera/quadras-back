ALTER TABLE court_bookings
    ADD COLUMN customer_whatsapp_number VARCHAR(20) NULL AFTER customer_name;

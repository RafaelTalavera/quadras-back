CREATE INDEX idx_reservations_date_status_slot
    ON reservations (reservation_date, status, start_time, end_time);

CREATE INDEX idx_court_bookings_date_status_slot
    ON court_bookings (booking_date, status, start_time, end_time);

CREATE INDEX idx_massage_bookings_provider_date_paid
    ON massage_bookings (provider_id, booking_date, paid);

CREATE INDEX idx_maintenance_orders_location_status_schedule
    ON maintenance_orders (location_id, status, scheduled_start_at, scheduled_end_at);

CREATE INDEX idx_maintenance_orders_status_reported_at
    ON maintenance_orders (status, reported_at);

CREATE INDEX idx_maintenance_order_attachments_order_created
    ON maintenance_order_attachments (maintenance_order_id, created_at, id);

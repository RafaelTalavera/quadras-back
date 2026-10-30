ALTER TABLE maintenance_orders
    ADD COLUMN schedule_version BIGINT NOT NULL DEFAULT 0 AFTER scheduled_end_at;

UPDATE maintenance_orders
SET schedule_version = 1
WHERE scheduled_start_at IS NOT NULL
  AND scheduled_end_at IS NOT NULL;

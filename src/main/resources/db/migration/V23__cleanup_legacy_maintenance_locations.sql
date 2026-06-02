DELETE FROM maintenance_orders
WHERE EXISTS (
    SELECT 1
    FROM maintenance_locations
    WHERE maintenance_locations.id = maintenance_orders.location_id
      AND (
          maintenance_locations.code IS NULL
          OR TRIM(maintenance_locations.code) NOT REGEXP '^[0-9]{2,3}$'
      )
);

DELETE FROM maintenance_locations
WHERE code IS NULL
   OR TRIM(code) NOT REGEXP '^[0-9]{2,3}$';

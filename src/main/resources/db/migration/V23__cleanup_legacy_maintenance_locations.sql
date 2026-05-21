DELETE FROM maintenance_orders
WHERE location_id IN (
    SELECT id
    FROM maintenance_locations
    WHERE code IS NULL
       OR NOT REGEXP_LIKE(TRIM(code), '^[0-9]{2,3}$')
);

DELETE FROM maintenance_locations
WHERE code IS NULL
   OR NOT REGEXP_LIKE(TRIM(code), '^[0-9]{2,3}$');

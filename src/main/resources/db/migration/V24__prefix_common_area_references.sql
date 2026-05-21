UPDATE maintenance_locations
SET label = CONCAT(TRIM(code), ' - ', TRIM(REGEXP_REPLACE(label, '^[0-9]{2,3}\\s*-\\s*', '')))
WHERE location_type = 'COMMON_AREA'
  AND REGEXP_LIKE(TRIM(code), '^[0-9]{2,3}$');

UPDATE maintenance_orders
SET location_label_snapshot = CONCAT(
    TRIM(location_code_snapshot),
    ' - ',
    TRIM(REGEXP_REPLACE(location_label_snapshot, '^[0-9]{2,3}\\s*-\\s*', ''))
)
WHERE location_type_snapshot = 'COMMON_AREA'
  AND REGEXP_LIKE(TRIM(location_code_snapshot), '^[0-9]{2,3}$');

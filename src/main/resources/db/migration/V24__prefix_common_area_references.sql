UPDATE maintenance_locations
SET label = CONCAT(
    TRIM(code),
    ' - ',
    CASE
        WHEN TRIM(label) REGEXP '^[0-9]{2,3}[[:space:]]*-[[:space:]]*'
            THEN TRIM(SUBSTRING(TRIM(label), LOCATE('-', TRIM(label)) + 1))
        ELSE TRIM(label)
    END
)
WHERE location_type = 'COMMON_AREA'
  AND TRIM(code) REGEXP '^[0-9]{2,3}$';

UPDATE maintenance_orders
SET location_label_snapshot = CONCAT(
    TRIM(location_code_snapshot),
    ' - ',
    CASE
        WHEN TRIM(location_label_snapshot) REGEXP '^[0-9]{2,3}[[:space:]]*-[[:space:]]*'
            THEN TRIM(SUBSTRING(TRIM(location_label_snapshot), LOCATE('-', TRIM(location_label_snapshot)) + 1))
        ELSE TRIM(location_label_snapshot)
    END
)
WHERE location_type_snapshot = 'COMMON_AREA'
  AND TRIM(location_code_snapshot) REGEXP '^[0-9]{2,3}$';

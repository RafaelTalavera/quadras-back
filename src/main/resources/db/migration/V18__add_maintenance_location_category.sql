ALTER TABLE maintenance_locations
    ADD COLUMN location_category VARCHAR(20) NULL;

UPDATE maintenance_locations
SET location_category = CASE
    WHEN location_type = 'COMMON_AREA' THEN 'COMMON_AREA'
    WHEN LOWER(CONCAT(COALESCE(code, ''), ' ', COALESCE(label, ''))) LIKE '%chalet%' THEN 'CHALET'
    ELSE 'APARTMENT'
END;

ALTER TABLE maintenance_locations
    MODIFY COLUMN location_category VARCHAR(20) NOT NULL;

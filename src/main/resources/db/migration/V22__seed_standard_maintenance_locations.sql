INSERT INTO maintenance_locations (
    location_type,
    location_category,
    code,
    label,
    floor,
    description,
    active,
    created_by,
    updated_by
)
SELECT 'COMMON_AREA', 'COMMON_AREA', '100', 'Areas comunes piso 1', '1',
       'Pasillos y areas comunes del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (
    SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '100'
);

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '101', 'Cuarto 101', '1', 'Cuarto 101 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '101');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '102', 'Cuarto 102', '1', 'Cuarto 102 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '102');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '103', 'Cuarto 103', '1', 'Cuarto 103 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '103');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '104', 'Cuarto 104', '1', 'Cuarto 104 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '104');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '105', 'Cuarto 105', '1', 'Cuarto 105 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '105');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '106', 'Cuarto 106', '1', 'Cuarto 106 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '106');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '107', 'Cuarto 107', '1', 'Cuarto 107 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '107');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '108', 'Cuarto 108', '1', 'Cuarto 108 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '108');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '109', 'Cuarto 109', '1', 'Cuarto 109 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '109');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '110', 'Cuarto 110', '1', 'Cuarto 110 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '110');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '111', 'Cuarto 111', '1', 'Cuarto 111 del primer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '111');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '200', 'Areas comunes piso 2', '2', 'Pasillos y areas comunes del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '200');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '201', 'Cuarto 201', '2', 'Cuarto 201 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '201');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '202', 'Cuarto 202', '2', 'Cuarto 202 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '202');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '203', 'Cuarto 203', '2', 'Cuarto 203 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '203');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '204', 'Cuarto 204', '2', 'Cuarto 204 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '204');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '205', 'Cuarto 205', '2', 'Cuarto 205 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '205');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '206', 'Cuarto 206', '2', 'Cuarto 206 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '206');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '207', 'Cuarto 207', '2', 'Cuarto 207 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '207');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '208', 'Cuarto 208', '2', 'Cuarto 208 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '208');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '209', 'Cuarto 209', '2', 'Cuarto 209 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '209');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '210', 'Cuarto 210', '2', 'Cuarto 210 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '210');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '211', 'Cuarto 211', '2', 'Cuarto 211 del segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '211');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '300', 'Areas comunes piso 3', '3', 'Pasillos y areas comunes del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '300');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '301', 'Cuarto 301', '3', 'Cuarto 301 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '301');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '302', 'Cuarto 302', '3', 'Cuarto 302 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '302');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '303', 'Cuarto 303', '3', 'Cuarto 303 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '303');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '304', 'Cuarto 304', '3', 'Cuarto 304 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '304');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '305', 'Cuarto 305', '3', 'Cuarto 305 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '305');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '306', 'Cuarto 306', '3', 'Cuarto 306 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '306');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '307', 'Cuarto 307', '3', 'Cuarto 307 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '307');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '308', 'Cuarto 308', '3', 'Cuarto 308 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '308');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '309', 'Cuarto 309', '3', 'Cuarto 309 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '309');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '310', 'Cuarto 310', '3', 'Cuarto 310 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '310');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '311', 'Cuarto 311', '3', 'Cuarto 311 del tercer piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '311');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '400', 'Areas comunes piso 4', '4', 'Pasillos y areas comunes del cuarto piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '400');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '401', 'Cuarto 401', '4', 'Cuarto 401 del cuarto piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '401');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '402', 'Cuarto 402', '4', 'Cuarto 402 del cuarto piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '402');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '403', 'Cuarto 403', '4', 'Cuarto 403 del cuarto piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '403');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '404', 'Cuarto 404', '4', 'Cuarto 404 del cuarto piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '404');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '405', 'Cuarto 405', '4', 'Cuarto 405 del cuarto piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '405');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '406', 'Cuarto 406', '4', 'Cuarto 406 del cuarto piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '406');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '407', 'Cuarto 407', '4', 'Cuarto 407 del cuarto piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '407');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '500', 'Recepcion', 'PB', 'Area de recepcion del hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '500');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '501', 'Reservas', 'PB', 'Area administrativa de reservas.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '501');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '502', 'Sala business', 'PB', 'Sala business del hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '502');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '503', 'Elevador', 'PB', 'Elevador principal del hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '503');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '504', 'Sala de TV', 'PB', 'Sala de TV para huespedes.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '504');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '505', 'Deposito eventos', 'PB', 'Deposito de eventos.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '505');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '506', 'Sala de juego', 'PB', 'Sala de juego y recreacion.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '506');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '507', 'Roperia central', 'PB', 'Roperia central del hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '507');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '508', 'Governanca', 'PB', 'Area de governanca.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '508');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '509', 'Oficinas RRHH-Financiero', 'PB', 'Oficinas administrativas de RRHH y financiero.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '509');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '510', 'Elevador de servicio', 'PB', 'Elevador de servicio.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '510');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '511', 'Almoxarifado', 'PB', 'Almoxarifado y deposito general.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '511');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '512', 'Cocina', 'PB', 'Cocina principal.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '512');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '513', 'Restaurante', 'PB', 'Area de restaurante.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '513');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '514', 'Piscina', 'PB', 'Piscina exterior.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '514');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '515', 'Piscina aquecida', 'PB', 'Piscina aquecida.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '515');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '516', 'Academia', 'PB', 'Academia del hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '516');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '517', 'Spa', 'PB', 'Area de spa.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '517');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '518', 'Sauna', 'PB', 'Area de sauna.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '518');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '519', 'Recreacion', 'PB', 'Area de recreacion.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '519');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '520', 'Estacionamiento', 'PB', 'Estacionamiento del hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '520');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '521', 'Entrada hotel', 'PB', 'Entrada principal del hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '521');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '522', 'Caldera', 'PB', 'Sala de caldera.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '522');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '523', 'Reservorio', 'PB', 'Reservorio del hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '523');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '524', 'Central electrica', 'PB', 'Central electrica.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '524');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '525', 'Camaras', 'PB', 'Sistema de camaras.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '525');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '526', 'Playa', 'PB', 'Area de playa.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '526');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '527', 'Jakusi', 'PB', 'Area de jakusi.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '527');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '528', 'Refectorio', 'PB', 'Refectorio.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '528');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '01', 'Chalet 01', 'Chalets', 'Chalet 01.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '01');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '02', 'Chalet 02', 'Chalets', 'Chalet 02.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '02');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '03', 'Chalet 03', 'Chalets', 'Chalet 03.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '03');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '04', 'Chalet 04', 'Chalets', 'Chalet 04.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '04');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '05', 'Chalet 05', 'Chalets', 'Chalet 05.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '05');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '06', 'Chalet 06', 'Chalets', 'Chalet 06.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '06');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '07', 'Chalet 07', 'Chalets', 'Chalet 07.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '07');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '08', 'Chalet 08', 'Chalets', 'Chalet 08.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '08');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '09', 'Chalet 09', 'Chalets', 'Chalet 09.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '09');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '10', 'Chalet 10', 'Chalets', 'Chalet 10.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '10');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '11', 'Chalet 11', 'Chalets', 'Chalet 11.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '11');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '12', 'Chalet 12', 'Chalets', 'Chalet 12.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '12');

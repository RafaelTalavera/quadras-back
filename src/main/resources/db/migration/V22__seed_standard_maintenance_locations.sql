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
SELECT 'COMMON_AREA', 'COMMON_AREA', '100', 'Areas comuns piso 1', '1',
       'Corredores e areas comuns do primeiro piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (
    SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '100'
);

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '101', 'Quarto 101', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '101');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '102', 'Quarto 102', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '102');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '103', 'Quarto 103', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '103');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '104', 'Quarto 104', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '104');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '105', 'Quarto 105', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '105');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '106', 'Quarto 106', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '106');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '107', 'Quarto 107', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '107');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '108', 'Quarto 108', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '108');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '109', 'Quarto 109', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '109');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '110', 'Quarto 110', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '110');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '111', 'Quarto 111', '1', 'Quarto do piso 1.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '111');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '200', 'Areas comuns piso 2', '2', 'Corredores e areas comuns do segundo piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '200');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '201', 'Quarto 201', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '201');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '202', 'Quarto 202', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '202');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '203', 'Quarto 203', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '203');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '204', 'Quarto 204', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '204');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '205', 'Quarto 205', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '205');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '206', 'Quarto 206', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '206');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '207', 'Quarto 207', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '207');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '208', 'Quarto 208', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '208');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '209', 'Quarto 209', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '209');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '210', 'Quarto 210', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '210');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '211', 'Quarto 211', '2', 'Quarto do piso 2.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '211');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '300', 'Areas comuns piso 3', '3', 'Corredores e areas comuns do terceiro piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '300');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '301', 'Quarto 301', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '301');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '302', 'Quarto 302', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '302');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '303', 'Quarto 303', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '303');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '304', 'Quarto 304', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '304');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '305', 'Quarto 305', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '305');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '306', 'Quarto 306', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '306');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '307', 'Quarto 307', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '307');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '308', 'Quarto 308', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '308');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '309', 'Quarto 309', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '309');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '310', 'Quarto 310', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '310');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '311', 'Quarto 311', '3', 'Quarto do piso 3.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '311');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '400', 'Areas comuns piso 4', '4', 'Corredores e areas comuns do quarto piso.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '400');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '401', 'Quarto 401', '4', 'Quarto do piso 4.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '401');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '402', 'Quarto 402', '4', 'Quarto do piso 4.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '402');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '403', 'Quarto 403', '4', 'Quarto do piso 4.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '403');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '404', 'Quarto 404', '4', 'Quarto do piso 4.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '404');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '405', 'Quarto 405', '4', 'Quarto do piso 4.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '405');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '406', 'Quarto 406', '4', 'Quarto do piso 4.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '406');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'APARTMENT', '407', 'Quarto 407', '4', 'Quarto do piso 4.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '407');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '500', 'Recepcao', 'PB', 'Area de recepcao do hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '500');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '501', 'Reservas', 'PB', 'Area administrativa de reservas.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '501');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '502', 'Sala business', 'PB', 'Sala business do hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '502');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '503', 'Elevador', 'PB', 'Elevador principal do hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '503');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '504', 'Sala de TV', 'PB', 'Sala de TV para hospedes.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '504');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '505', 'Deposito de eventos', 'PB', 'Deposito de eventos.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '505');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '506', 'Sala de jogos', 'PB', 'Sala de jogos e recreacao.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '506');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '507', 'Rouparia central', 'PB', 'Rouparia central do hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '507');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '508', 'Governanca', 'PB', 'Area de governanca.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '508');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '509', 'Escritorios RH-Financeiro', 'PB', 'Escritorios administrativos de RH e financeiro.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '509');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '510', 'Elevador de servico', 'PB', 'Elevador de servico.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '510');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '511', 'Almoxarifado', 'PB', 'Almoxarifado e deposito geral.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '511');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '512', 'Cozinha', 'PB', 'Cozinha principal.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '512');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '513', 'Restaurante', 'PB', 'Area de restaurante.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '513');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '514', 'Piscina', 'PB', 'Piscina externa.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '514');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '515', 'Piscina aquecida', 'PB', 'Piscina aquecida.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '515');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '516', 'Academia', 'PB', 'Academia do hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '516');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '517', 'Spa', 'PB', 'Area de spa.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '517');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '518', 'Sauna', 'PB', 'Area de sauna.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '518');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '519', 'Recreacao', 'PB', 'Area de recreacao.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '519');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '520', 'Estacionamento', 'PB', 'Estacionamento do hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '520');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '521', 'Entrada do hotel', 'PB', 'Entrada principal do hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '521');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '522', 'Caldeira', 'PB', 'Sala de caldeira.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '522');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '523', 'Reservatorio', 'PB', 'Reservatorio do hotel.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '523');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '524', 'Central eletrica', 'PB', 'Central eletrica.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '524');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '525', 'Cameras', 'PB', 'Sistema de cameras.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '525');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '526', 'Praia', 'PB', 'Area de praia.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '526');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '527', 'Jacuzzi', 'PB', 'Area de jacuzzi.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '527');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'COMMON_AREA', 'COMMON_AREA', '528', 'Refeitorio', 'PB', 'Refeitorio.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'COMMON_AREA' AND code = '528');

INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '01', 'Chale 01', 'Chales', 'Unidade de hospedagem chale 01.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '01');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '02', 'Chale 02', 'Chales', 'Unidade de hospedagem chale 02.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '02');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '03', 'Chale 03', 'Chales', 'Unidade de hospedagem chale 03.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '03');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '04', 'Chale 04', 'Chales', 'Unidade de hospedagem chale 04.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '04');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '05', 'Chale 05', 'Chales', 'Unidade de hospedagem chale 05.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '05');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '06', 'Chale 06', 'Chales', 'Unidade de hospedagem chale 06.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '06');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '07', 'Chale 07', 'Chales', 'Unidade de hospedagem chale 07.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '07');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '08', 'Chale 08', 'Chales', 'Unidade de hospedagem chale 08.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '08');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '09', 'Chale 09', 'Chales', 'Unidade de hospedagem chale 09.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '09');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '10', 'Chale 10', 'Chales', 'Unidade de hospedagem chale 10.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '10');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '11', 'Chale 11', 'Chales', 'Unidade de hospedagem chale 11.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '11');
INSERT INTO maintenance_locations (location_type, location_category, code, label, floor, description, active, created_by, updated_by)
SELECT 'ROOM', 'CHALET', '12', 'Chale 12', 'Chales', 'Unidade de hospedagem chale 12.', TRUE, 'system.catalog', 'system.catalog'
WHERE NOT EXISTS (SELECT 1 FROM maintenance_locations WHERE location_type = 'ROOM' AND code = '12');

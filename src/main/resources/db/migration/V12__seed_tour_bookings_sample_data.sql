INSERT INTO tour_bookings (
    service_type,
    start_at,
    end_at,
    client_name,
    guest_reference,
    provider_id,
    provider_offering_id,
    provider_offering_name,
    amount,
    commission_percent,
    commission_amount,
    description,
    paid,
    payment_method,
    payment_date,
    payment_notes,
    status,
    cancellation_notes,
    created_by,
    updated_by
)
SELECT
    seed.service_type,
    seed.start_at,
    seed.end_at,
    seed.client_name,
    seed.guest_reference,
    provider.id,
    offering.id,
    offering.name,
    seed.amount,
    provider.default_commission_percent,
    ROUND(seed.amount * provider.default_commission_percent / 100, 2),
    seed.description,
    seed.paid,
    seed.payment_method,
    seed.payment_date,
    seed.payment_notes,
    seed.status,
    seed.cancellation_notes,
    'system',
    'system'
FROM (
    SELECT 'Agencia Costa Norte' AS provider_name, 'Isla de Campeche con desembarco' AS offering_name, 'TOUR' AS service_type,
           TIMESTAMP '2026-02-03 08:30:00' AS start_at, TIMESTAMP '2026-02-03 14:00:00' AS end_at,
           'Helena Duarte' AS client_name, 'Apto 101' AS guest_reference, 350.00 AS amount,
           'Passeio com desembarque e traslado ao porto.' AS description, TRUE AS paid, 'PIX' AS payment_method,
           DATE '2026-02-03' AS payment_date, 'Pago no check-in.' AS payment_notes, 'SCHEDULED' AS status,
           NULL AS cancellation_notes
    UNION ALL
    SELECT 'Traslados Ilha Sul', 'Traslado Aeropuerto', 'TRAVEL',
           TIMESTAMP '2026-02-08 11:15:00', TIMESTAMP '2026-02-08 12:30:00',
           'Marco Silveira', 'Casa 02', 190.00,
           'Chegada aeroporto internacional, 3 passageiros.', FALSE, NULL,
           NULL, NULL, 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Agencia Costa Norte', 'Tour a Camburiu', 'TOUR',
           TIMESTAMP '2026-02-21 07:00:00', TIMESTAMP '2026-02-21 19:30:00',
           'Beatriz Lemos', 'Apto 207', 150.00,
           'City tour com paradas e retorno ao hotel.', TRUE, 'CARD',
           DATE '2026-02-20', 'Cobrado antecipado.', 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Traslados Ilha Sul', 'Traslado Centro', 'TRAVEL',
           TIMESTAMP '2026-03-04 09:00:00', TIMESTAMP '2026-03-04 10:00:00',
           'Diego Rocha', 'Apto 305', 120.00,
           'Saida para compras no centro.', FALSE, NULL,
           NULL, NULL, 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Agencia Costa Norte', 'Isla de Campeche con desembarco', 'TOUR',
           TIMESTAMP '2026-03-12 08:00:00', TIMESTAMP '2026-03-12 15:00:00',
           'Camila Freitas', 'Apto 110', 350.00,
           'Passeio reservado para casal.', TRUE, 'TRANSFER',
           DATE '2026-03-10', 'Transferencia confirmada.', 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Traslados Ilha Sul', 'Traslado Canasvieira', 'TRAVEL',
           TIMESTAMP '2026-03-27 18:30:00', TIMESTAMP '2026-03-27 19:10:00',
           'Luciana Braga', 'Casa 07', 90.00,
           'Traslado noturno ate Canasvieira.', FALSE, NULL,
           NULL, NULL, 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Agencia Costa Norte', 'Tour a Camburiu', 'TOUR',
           TIMESTAMP '2026-05-02 06:45:00', TIMESTAMP '2026-05-02 19:15:00',
           'Paulo Mendes', 'Apto 208', 150.00,
           'Tour em grupo pequeno.', TRUE, 'PIX',
           DATE '2026-05-02', 'Pix confirmado no embarque.', 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Traslados Ilha Sul', 'Traslado Aeropuerto', 'TRAVEL',
           TIMESTAMP '2026-05-05 13:20:00', TIMESTAMP '2026-05-05 14:35:00',
           'Renata Farias', 'Apto 105', 190.00,
           'Saida para aeroporto com duas malas grandes.', TRUE, 'CASH',
           DATE '2026-05-05', 'Pago direto ao concierge.', 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Agencia Costa Norte', 'Isla de Campeche con desembarco', 'TOUR',
           TIMESTAMP '2026-05-11 08:15:00', TIMESTAMP '2026-05-11 14:40:00',
           'Thiago Costa', 'Cobertura 01', 350.00,
           'Passeio familia com criancas.', FALSE, NULL,
           NULL, NULL, 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Traslados Ilha Sul', 'Traslado Centro', 'TRAVEL',
           TIMESTAMP '2026-05-18 10:00:00', TIMESTAMP '2026-05-18 10:50:00',
           'Sandra Araujo', 'Apto 212', 120.00,
           'Traslado para almoco e compras.', FALSE, NULL,
           NULL, NULL, 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Agencia Costa Norte', 'Tour a Camburiu', 'TOUR',
           TIMESTAMP '2026-05-27 07:30:00', TIMESTAMP '2026-05-27 19:00:00',
           'Eduardo Nunes', 'Apto 410', 150.00,
           'Tour completo com parada para fotos.', TRUE, 'CARD',
           DATE '2026-05-27', 'Cartao processado no checkout.', 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Traslados Ilha Sul', 'Traslado Aeropuerto', 'TRAVEL',
           TIMESTAMP '2026-06-01 05:30:00', TIMESTAMP '2026-06-01 06:45:00',
           'Natalia Pires', 'Apto 103', 190.00,
           'Transfer madrugador para voo nacional.', TRUE, 'PIX',
           DATE '2026-06-01', 'Pago antecipado.', 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Agencia Costa Norte', 'Isla de Campeche con desembarco', 'TOUR',
           TIMESTAMP '2026-06-04 08:10:00', TIMESTAMP '2026-06-04 15:10:00',
           'Joao Teles', 'Apto 115', 350.00,
           'Reserva de casal com desembarque.', FALSE, NULL,
           NULL, NULL, 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Traslados Ilha Sul', 'Traslado Centro', 'TRAVEL',
           TIMESTAMP '2026-06-09 14:00:00', TIMESTAMP '2026-06-09 14:50:00',
           'Marina Castro', 'Casa 04', 120.00,
           'Saida para centro historico.', FALSE, NULL,
           NULL, NULL, 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Agencia Costa Norte', 'Tour a Camburiu', 'TOUR',
           TIMESTAMP '2026-06-12 07:00:00', TIMESTAMP '2026-06-12 19:20:00',
           'Ricardo Mattos', 'Apto 305', 150.00,
           'Passeio com casal e filho.', TRUE, 'TRANSFER',
           DATE '2026-06-11', 'Transfer confirmado.', 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Traslados Ilha Sul', 'Traslado Canasvieira', 'TRAVEL',
           TIMESTAMP '2026-06-16 16:20:00', TIMESTAMP '2026-06-16 17:00:00',
           'Fernanda Luz', 'Apto 118', 90.00,
           'Traslado rapido ao fim da tarde.', TRUE, 'CASH',
           DATE '2026-06-16', 'Pago em dinheiro.', 'SCHEDULED',
           NULL
    UNION ALL
    SELECT 'Agencia Costa Norte', 'Isla de Campeche con desembarco', 'TOUR',
           TIMESTAMP '2026-06-22 08:20:00', TIMESTAMP '2026-06-22 15:30:00',
           'Patricia Ramos', 'Apto 222', 350.00,
           'Passeio sujeito ao clima.', FALSE, NULL,
           NULL, NULL, 'CANCELLED',
           'Cancelado pela agencia por vento forte.'
    UNION ALL
    SELECT 'Traslados Ilha Sul', 'Traslado Aeropuerto', 'TRAVEL',
           TIMESTAMP '2026-06-28 12:10:00', TIMESTAMP '2026-06-28 13:20:00',
           'Gustavo Meireles', 'Casa 09', 190.00,
           'Saida aeroporto para 4 passageiros.', FALSE, NULL,
           NULL, NULL, 'SCHEDULED',
           NULL
) seed
JOIN tour_providers provider
    ON provider.name = seed.provider_name
JOIN tour_provider_offerings offering
    ON offering.provider_id = provider.id
   AND offering.name = seed.offering_name;

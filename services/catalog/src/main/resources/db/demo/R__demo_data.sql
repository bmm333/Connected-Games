-- Demo data for local development (same names as the design sample data).


INSERT INTO organization (id, name) VALUES
    ('a0000000-0000-4000-8000-000000000001', 'Gruppo Belvedere'),
    ('a0000000-0000-4000-8000-000000000002', 'Circolo Aurora APS')
ON CONFLICT (id) DO NOTHING;

INSERT INTO venue (id, organization_id, name, address, city, type) VALUES
    ('b0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', 'Bar Belvedere', 'Via del Belvedere 12', 'Roma', 'PUBLIC'),
    ('b0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000002', 'Circolo Aurora', 'Corso Aurora 5', 'Torino', 'PUBLIC')
ON CONFLICT (id) DO NOTHING;

INSERT INTO game_type (id, name, mode, participants_per_side, points_to_win) VALUES
    ('c0000000-0000-4000-8000-000000000001', 'Calciobalilla', 'TEAM', 2, 10),
    ('c0000000-0000-4000-8000-000000000002', 'Scacchi', 'INDIVIDUAL', 1, 1),
    ('c0000000-0000-4000-8000-000000000003', 'Freccette', 'INDIVIDUAL', 1, 501)
ON CONFLICT (id) DO NOTHING;

INSERT INTO game_table (id, venue_id, game_type_id, name, qr_code, status) VALUES
    ('d0000000-0000-4000-8000-000000000001', 'b0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000001', 'Calciobalilla verde - Tavolo 1', 'belvedere-t1', 'FREE'),
    ('d0000000-0000-4000-8000-000000000002', 'b0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000001', 'Calciobalilla verde - Tavolo 2', 'belvedere-t2', 'OCCUPIED'),
    ('d0000000-0000-4000-8000-000000000003', 'b0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000001', 'Calciobalilla verde - Tavolo 3', 'belvedere-t3', 'FREE'),
    ('d0000000-0000-4000-8000-000000000004', 'b0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000001', 'Calciobalilla verde - Tavolo 4', 'belvedere-t4', 'MAINTENANCE'),
    ('d0000000-0000-4000-8000-000000000005', 'b0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000002', 'Scacchi - Tavolo 1', 'aurora-chess-t1', 'FREE')
ON CONFLICT (id) DO NOTHING;

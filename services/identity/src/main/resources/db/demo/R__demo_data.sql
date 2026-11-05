-- Demo users for local development. Every password is "password123" (bcrypt below).
-- The venue and the organization are the Bar Belvedere ones of catalog's demo data.

INSERT INTO user_account (id, email, display_name, password_hash, player_id) VALUES
    ('e0000000-0000-4000-8000-000000000001', 'gestore@belvedere.example', 'Gestore Belvedere',
     '$2b$10$lgGqP1uRKcmJsvKVkhxMRuzWApi8keMOIo7cnWv8FplIZoaUx7xv.', 'f0000000-0000-4000-8000-000000000001'),
    ('e0000000-0000-4000-8000-000000000002', 'giulia@example.com', 'Giulia Ferri',
     '$2b$10$lgGqP1uRKcmJsvKVkhxMRuzWApi8keMOIo7cnWv8FplIZoaUx7xv.', 'f0000000-0000-4000-8000-000000000002'),
    ('e0000000-0000-4000-8000-000000000003', 'admin@connectedgames.example', 'Admin Piattaforma',
     '$2b$10$lgGqP1uRKcmJsvKVkhxMRuzWApi8keMOIo7cnWv8FplIZoaUx7xv.', 'f0000000-0000-4000-8000-000000000003')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_role (user_id, position, role, venue_id, organization_id) VALUES
    ('e0000000-0000-4000-8000-000000000001', 0, 'PLAYER', NULL, NULL),
    ('e0000000-0000-4000-8000-000000000001', 1, 'VENUE_MANAGER',
     'b0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001'),
    ('e0000000-0000-4000-8000-000000000002', 0, 'PLAYER', NULL, NULL),
    ('e0000000-0000-4000-8000-000000000003', 0, 'PLATFORM_ADMIN', NULL, NULL)
ON CONFLICT (user_id, position) DO NOTHING;

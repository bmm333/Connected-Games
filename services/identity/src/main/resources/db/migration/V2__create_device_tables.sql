-- identity: paired table displays and the pairing codes that create them

-- venue_id and table_id are ids of catalog: no foreign key across services
CREATE TABLE device (
    id uuid PRIMARY KEY,
    venue_id uuid NOT NULL,
    table_id uuid NOT NULL,
    surface text NOT NULL CHECK (surface IN ('KIOSK')),
    paired_at timestamptz NOT NULL,
    -- one display per surface at each table, pairing a new one , removes the old
    UNIQUE (table_id, surface)
);

-- the code
CREATE TABLE pairing_code (
    code    text    PRIMARY KEY CHECK (code ~ '^[A-Z0-9]{6}$'),
    venue_id    uuid    NOT NULL,
    table_id    uuid    NOT NULL,
    surface text    NOT NULL CHECK (surface IN ('KIOSK')),
    expires_at  timestamptz NOT NULL,
    used_at timestamptz
);

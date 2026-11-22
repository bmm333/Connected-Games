-- match: game type, tournament link and tenant, as in ADR012

DELETE FROM match_side;
DELETE FROM match;
DELETE FROM match_event;

-- ids of other services: no foreign key. tenant_id is the organization of the venue, as declared by the hub.
ALTER TABLE match
    ADD COLUMN tenant_id text NOT NULL CHECK (tenant_id <> ''),
    ADD COLUMN game_type_id uuid NOT NULL,
    -- null for a friendly or offline match
    ADD COLUMN tournament_match_id uuid;

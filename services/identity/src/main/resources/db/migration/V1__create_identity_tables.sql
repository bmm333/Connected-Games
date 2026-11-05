-- identity: user accounts with their roles, guests, sessions and refresh tokens

CREATE TABLE user_account (
    id  uuid    PRIMARY KEY,
    -- already lower case: the domain normalizes it, so a plain UNIQUE is enough
    email   text    NOT NULL UNIQUE,
    display_name    text    NOT NULL,
    password_hash   text    NOT NULL,
    player_id   uuid    NOT NULL UNIQUE
);

-- venue and organization are ids of catalog: no foreign key across services
CREATE TABLE user_role (
    user_id uuid    NOT NULL REFERENCES user_account (id) ON DELETE CASCADE,
    position    integer NOT NULL,
    role    text    NOT NULL CHECK (role IN ('PLAYER','VENUE_MANAGER','GAME_ADMIN','PLATFORM_ADMIN')),
    venue_id    uuid,
    organization_id uuid,
    PRIMARY KEY (user_id, position),
    CHECK ((role = 'VENUE_MANAGER' AND venue_id IS NOT NULL AND organization_id IS NOT NULL)
        OR (role <> 'VENUE_MANAGER' AND venue_id IS NULL AND organization_id IS NULL))
);

CREATE TABLE guest (
    player_id   uuid    PRIMARY KEY,
    nickname    text    NOT NULL
);

-- subject_id is a user, a guest player or a device depending on kind: no foreign key
CREATE TABLE session (
    id  uuid    PRIMARY KEY,
    kind    text    NOT NULL CHECK (kind IN ('GUEST','USER','DEVICE')),
    subject_id  uuid    NOT NULL,
    started_at  timestamptz NOT NULL,
    refresh_expires_at  timestamptz NOT NULL,
    revoked_at  timestamptz,
    CHECK (refresh_expires_at > started_at)
);

CREATE INDEX session_subject_idx ON session (subject_id);

-- only the SHA-256 of the token, never the token itself
CREATE TABLE refresh_token (
    token_hash  text    PRIMARY KEY,
    session_id  uuid    NOT NULL REFERENCES session (id),
    issued_at   timestamptz NOT NULL,
    used_at timestamptz
);

CREATE INDEX refresh_token_session_idx ON refresh_token (session_id);

-- match: the matches as the cloud sees them, and the log of every event received

-- venue and table are ids of catalog: no foreign key across services
CREATE TABLE match (
    id  uuid    PRIMARY KEY,
    venue_id    uuid    NOT NULL,
    table_id    uuid    NOT NULL,
    status  text    NOT NULL CHECK (status IN ('IN_PROGRESS','COMPLETED','CANCELLED')),
    mode    text    NOT NULL CHECK (mode IN ('INDIVIDUAL','TEAM')),
    points_to_win   integer NOT NULL CHECK (points_to_win >= 1),
    winner_side text    CHECK (winner_side IN ('RED','BLUE')),
    started_at  timestamptz NOT NULL,
    ended_at    timestamptz
);

-- the current match of a table: what the QR page asks for
CREATE INDEX match_table_status_idx ON match (table_id, status);

-- the two sides of a match; "position" keeps their order
CREATE TABLE match_side (
    match_id    uuid    NOT NULL REFERENCES match (id) ON DELETE CASCADE,
    position    integer NOT NULL,
    side    text    NOT NULL CHECK (side IN ('RED','BLUE')),
    participant_id  text,
    display_name    text,
    score   integer NOT NULL CHECK (score >= 0),
    PRIMARY KEY (match_id, position),
    UNIQUE (match_id, side)
);

-- the event log: rows are never updated. No foreign key to match: an event may arrive before its start.
-- What is specific to the type of event is in payload, as JSON text.
CREATE TABLE match_event (
    event_id    uuid    PRIMARY KEY,
    venue_id    uuid    NOT NULL,
    table_id    uuid    NOT NULL,
    match_id    uuid    NOT NULL,
    seq integer NOT NULL CHECK (seq >= 1),
    type    text    NOT NULL CHECK (type IN ('MatchStarted','GoalScored','MatchEnded')),
    occurred_at timestamptz NOT NULL,
    payload text    NOT NULL,
    -- a match is venue + match id: an event of another venue cannot take the seq of this match
    UNIQUE (venue_id, match_id, seq)
);

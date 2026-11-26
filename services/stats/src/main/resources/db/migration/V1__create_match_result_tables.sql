-- stats: the closed matches received from the match service (MatchCompleted), the facts the statistics come from.
-- One row per match: matchId is the key, because the broker delivers at least once. Cancelled matches are not saved.

CREATE TABLE match_result (
    match_id    uuid    PRIMARY KEY,
    -- id of the message, for the logs
    message_id  uuid    NOT NULL,
    tenant_id   text    NOT NULL CHECK (tenant_id <> ''),
    venue_id    uuid    NOT NULL,
    table_id    uuid    NOT NULL,
    game_type_id    uuid    NOT NULL,
    tournament_match_id uuid,
    mode    text    NOT NULL CHECK (mode IN ('INDIVIDUAL','TEAM')),
    points_to_win   integer NOT NULL CHECK (points_to_win >= 1),
    reason  text    NOT NULL CHECK (reason IN ('SCORE_REACHED','TIME_EXPIRED')),
    -- missing for a draw
    winner_side text    CHECK (winner_side IN ('RED','BLUE')),
    started_at  timestamptz NOT NULL,
    ended_at    timestamptz NOT NULL,
    received_at timestamptz NOT NULL DEFAULT now(),
    CHECK (ended_at >= started_at)
);

CREATE TABLE match_side_result (
    match_id    uuid    NOT NULL REFERENCES match_result (match_id) ON DELETE CASCADE,
    side    text    NOT NULL CHECK (side IN ('RED','BLUE')),
    -- team or player: missing if the match was played without login
    participant_id  text    CHECK (participant_id <> ''),
    display_name    text,
    score   integer NOT NULL CHECK (score >= 0),
    PRIMARY KEY (match_id, side)
);

-- The points the service received, one by one. They can be fewer than the score: the score is the one that counts.
CREATE TABLE scored_point (
    match_id    uuid    NOT NULL REFERENCES match_result (match_id) ON DELETE CASCADE,
    seq integer NOT NULL CHECK (seq >= 1),
    side    text    NOT NULL CHECK (side IN ('RED','BLUE')),
    occurred_at timestamptz NOT NULL,
    PRIMARY KEY (match_id, seq)
);

-- Every query starts from the tenant
CREATE INDEX match_result_tenant_ended_idx ON match_result (tenant_id, ended_at DESC);
CREATE INDEX match_result_tenant_venue_game_idx ON match_result (tenant_id, venue_id, game_type_id);
CREATE INDEX match_side_result_participant_idx ON match_side_result (participant_id) WHERE participant_id IS NOT NULL;

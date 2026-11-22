-- match: why a match ended, and the messages for the other services (ADR011)

ALTER TABLE match
    ADD COLUMN end_reason text CHECK (end_reason IN ('SCORE_REACHED','TIME_EXPIRED','CANCELLED'));

-- a match in progress has no reason, a closed one always has it
ALTER TABLE match
    ADD CONSTRAINT match_end_reason_closed_check CHECK ((status = 'IN_PROGRESS') = (end_reason IS NULL));

-- Written in the same transaction that changes the match. A publisher sends the rows with published_at null,
-- in seq order, and then sets published_at. Rows are kept: a consumer that was not there can be fed again.
CREATE TABLE outbox_event (
    seq bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id    uuid    NOT NULL UNIQUE,
    type    text    NOT NULL CHECK (type IN ('MatchCompleted')),
    -- matchId is the key of the consumers: one message of each type for each match
    match_id    uuid    NOT NULL,
    -- the message as the service stored it, as JSON text
    payload text    NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    published_at    timestamptz,
    UNIQUE (type, match_id)
);

-- the publisher only looks at what is still to send
CREATE INDEX outbox_event_unpublished_idx ON outbox_event (seq) WHERE published_at IS NULL;

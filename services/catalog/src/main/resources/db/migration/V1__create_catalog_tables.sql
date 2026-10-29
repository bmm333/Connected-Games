-- catalog of the platform: organizations (tenants) , venues, game types and tables

CREATE TABLE organization (
    id  uuid    PRIMARY KEY,
    name    text    NOT NULL
);

CREATE TABLE venue(
    id  uuid    PRIMARY KEY,
    organization_id uuid    NOT NULL REFERENCES organization   (id),
    name    text    NOT NULL,
    address text    NOT NULL,
    city    text    NOT NULL,
    type    text    NOT NULL CHECK (type IN ('PUBLIC','PRIVATE'))
);

CREATE INDEX venue_city_idx ON venue (lower(city));

CREATE TABLE game_type(
    id  uuid    PRIMARY KEY,
    name    text    NOT NULL UNIQUE,
    mode    text    NOT NULL CHECK (mode in ('INDIVIDUAL','TEAM')),
    participants_per_side   integer NOT NULL CHECK (participants_per_side>=1),
    points_to_win   integer     NOT NULL CHECK (points_to_win>=1)
);

-- cannot use name directly "table" since its reserved in sql
CREATE TABLE game_table(
    id  uuid    PRIMARY KEY,
    venue_id    uuid    NOT NULL REFERENCES venue (id),
    game_type_id    uuid    NOT NULL REFERENCES game_type (id),
    name    text    NOT NULL,
    qr_code text NOT NULL UNIQUE,
    status  text    NOT NULL CHECK (status IN ('FREE','OCCUPIED','OFFLINE','MAINTENANCE'))
);

CREATE INDEX game_table_venue_idx ON game_table (venue_id);

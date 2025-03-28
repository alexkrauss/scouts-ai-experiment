CREATE TABLE events (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    version bigint NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    meeting_point VARCHAR(512) NOT NULL,
    location VARCHAR(512) NOT NULL,
    cost VARCHAR(100) NOT NULL,
    additional_info text NOT NULL
);

CREATE TABLE event_groups (
    event_id bigint NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    group_id bigint NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, group_id)
);

CREATE TABLE registrations (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    version bigint NOT NULL DEFAULT 0,
    scout_id bigint NOT NULL REFERENCES scouts (id) ON DELETE CASCADE,
    event_id bigint NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    note text NOT NULL,
    status VARCHAR(20) NOT NULL,
    registration_date timestamp NOT NULL,
    account_id VARCHAR(255) NOT NULL
);

CREATE INDEX idx_registrations_scout_id ON registrations (scout_id);
CREATE INDEX idx_registrations_event_id ON registrations (event_id);
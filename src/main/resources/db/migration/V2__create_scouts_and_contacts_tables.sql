CREATE TABLE scouts (
    id bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    version bigint NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    address VARCHAR(512) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    health_insurance VARCHAR(255) NOT NULL,
    allergy_info text NOT NULL,
    vaccination_info text NOT NULL,
    last_updated DATE NOT NULL
);

CREATE TABLE scout_contacts (
    scout_id bigint NOT NULL REFERENCES scouts (id),
    contact_order INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    relationship VARCHAR(100) NOT NULL,
    PRIMARY KEY (scout_id, contact_order)
);

CREATE TABLE scout_groups (
    scout_id bigint NOT NULL REFERENCES scouts (id),
    group_id bigint NOT NULL REFERENCES groups (id),
    PRIMARY KEY (scout_id, group_id)
);

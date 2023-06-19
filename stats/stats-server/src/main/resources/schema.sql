DROP TABLE IF EXISTS hits CASCADE;

-- Table for hits
CREATE TABLE IF NOT EXISTS hits (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    app VARCHAR(512) NOT NULL,
    uri VARCHAR(512) NOT NULL,
    ip VARCHAR(512) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_hit PRIMARY KEY (id)
);
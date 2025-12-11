CREATE TABLE clients
(
    uuid  BLOB PRIMARY KEY,
    owner INTEGER NOT NULL,
    last_action_applied INTEGER
) STRICT;

CREATE TABLE IF NOT EXISTS syncengine_store
(
    key   TEXT PRIMARY KEY,
    value TEXT
) STRICT;
CREATE TABLE IF NOT EXISTS clients
(
    uuid  BLOB PRIMARY KEY,
    owner INTEGER NOT NULL,
    last_action_applied INTEGER,
    last_frame_seen     INTEGER
) STRICT;

CREATE TABLE IF NOT EXISTS syncengine_store
(
    key   TEXT PRIMARY KEY,
    value TEXT
) STRICT;

CREATE TABLE IF NOT EXISTS workspaces
(
    uuid  BLOB PRIMARY KEY,
    owner INTEGER NOT NULL,
    name  TEXT
) STRICT;
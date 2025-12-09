CREATE TABLE clients
(
    uuid                 BLOB PRIMARY KEY,
    owner                INTEGER NOT NULL,
    last_action_applied INTEGER
) STRICT;
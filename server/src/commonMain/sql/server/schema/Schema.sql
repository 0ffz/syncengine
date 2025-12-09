CREATE TABLE clients
(
    uuid                 BLOB PRIMARY KEY,
    owner                INTEGER NOT NULL,
    last_mutator_applied INTEGER
) STRICT;
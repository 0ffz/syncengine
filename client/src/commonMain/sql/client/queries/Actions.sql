-- fun updateLastAction(json: String)
UPDATE actions_list
SET data = jsonb(:json)
WHERE id = ( SELECT max(id) FROM actions_list );

-- fun append(json: String)
INSERT INTO actions_list(data)
VALUES (jsonb(:json));

-- getAll
SELECT data
FROM actions_list;

-- fun clearAcknowledged(id: Long)
DELETE
FROM actions_list
WHERE id <= :id;

-- firstActionId
SELECT min(id)
FROM actions_list;
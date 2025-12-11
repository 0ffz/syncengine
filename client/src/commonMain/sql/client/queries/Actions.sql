-- fun updateLastAction(bytes: ByteArray)
UPDATE actions_list
SET data = :bytes
WHERE id = ( SELECT max(id) FROM actions_list );

-- fun append(bytes: ByteArray)
INSERT INTO actions_list(data)
VALUES (:bytes);

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
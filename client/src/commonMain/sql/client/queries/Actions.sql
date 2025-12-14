-- fun updateLastAction(bytes: ByteArray)
UPDATE actions_list
SET data = :bytes
WHERE id = ( SELECT max(id) FROM actions_list );

-- fun append(bytes: ByteArray)
INSERT INTO actions_list(data)
VALUES (:bytes);

-- fun getAllAfterId(id: Long)
SELECT data
FROM actions_list
WHERE id > :id
ORDER BY id;

-- fun clearAcknowledged(id: Long)
DELETE
FROM actions_list
WHERE id <= :id;

-- firstActionId
SELECT min(id) AS min
FROM actions_list;

-- count
SELECT count(ROWID) AS count
FROM actions_list;
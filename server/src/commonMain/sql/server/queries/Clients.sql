-- fun getLastActionApplied(uuid: kotlin.uuid.Uuid, owner: Long)
SELECT last_action_applied
FROM clients
WHERE uuid = :uuid
  AND owner = :owner;

-- fun setLastActionApplied(uuid: kotlin.uuid.Uuid, lastMutatorApplied: Long, owner: Long)
INSERT OR
REPLACE INTO clients (uuid, owner, last_action_applied)
VALUES (:uuid, :owner, :lastMutatorApplied);


-- fun setLastFrameSeen(uuid: kotlin.uuid.Uuid, lastFrameSeen: Long, owner: Long)
-- UPDATE clients
-- SET last_frame_seen = :lastFrameSeen
-- WHERE uuid = :uuid
--   AND owner = :owner;
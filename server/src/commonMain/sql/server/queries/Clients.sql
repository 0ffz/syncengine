-- fun getLastActionApplied(uuid: kotlin.uuid.Uuid, owner: Long)
SELECT last_action_applied
FROM clients
WHERE uuid = :uuid
  AND owner = :owner;

-- fun setLastActionApplied(uuid: kotlin.uuid.Uuid, lastMutatorApplied: Long, owner: Long)
UPDATE clients
SET last_action_applied = :lastMutatorApplied
WHERE uuid = :uuid
  AND owner = :owner;
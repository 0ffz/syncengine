-- fun getLastMutatorApplied(uuid: kotlin.uuid.Uuid, owner: Long)
SELECT last_mutator_applied
FROM clients
WHERE uuid = :uuid
  AND owner = :owner;

-- fun setLastMutatorApplied(uuid: kotlin.uuid.Uuid, lastMutatorApplied: Long, owner: Long)
UPDATE clients
SET last_mutator_applied = :lastMutatorApplied
WHERE uuid = :uuid
  AND owner = :owner;
-- Collapse duplicate role assignments left by the pre-origin-aware addUserToRole,
-- keeping the lowest id of each (user_id, role_id, origin) group.

DELETE FROM ${ohdsiSchema}.sec_user_role
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               row_number() OVER (PARTITION BY user_id, role_id, origin ORDER BY id) AS rn
        FROM ${ohdsiSchema}.sec_user_role
    ) ranked
    WHERE ranked.rn > 1
);

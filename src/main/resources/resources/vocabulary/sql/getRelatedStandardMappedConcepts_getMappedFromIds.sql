SELECT
   c.CONCEPT_ID,
   c.MAPPED_FROM_ID
FROM (
    SELECT DISTINCT
        c.CONCEPT_ID,
        CAST(cr.CONCEPT_ID_1 AS VARCHAR) as MAPPED_FROM_ID
    FROM
        @CDM_schema.concept_relationship cr
    JOIN
        @CDM_schema.concept c ON cr.CONCEPT_ID_2 = c.CONCEPT_ID
    JOIN
        @CDM_schema.relationship r ON cr.RELATIONSHIP_ID = r.RELATIONSHIP_ID
    WHERE
        cr.CONCEPT_ID_1 IN (@conceptIdList)
        AND COALESCE(c.STANDARD_CONCEPT, 'N') IN ('S', 'C')
        AND cr.INVALID_REASON IS NULL
        AND cr.relationship_id = 'Maps to'
) c
ORDER BY
   c.CONCEPT_ID;
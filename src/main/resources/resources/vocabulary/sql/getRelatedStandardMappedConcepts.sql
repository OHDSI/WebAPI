SELECT
   c.CONCEPT_ID,
   c.CONCEPT_NAME,
   COALESCE(c.STANDARD_CONCEPT, 'N') as STANDARD_CONCEPT,
   COALESCE(c.INVALID_REASON, 'V') as INVALID_REASON,
   c.CONCEPT_CODE,
   c.CONCEPT_CLASS_ID,
   c.DOMAIN_ID,
   c.VOCABULARY_ID,
   c.VALID_START_DATE,
   c.VALID_END_DATE,
   c.RELATIONSHIP_NAME,
   c.RELATIONSHIP_DISTANCE,
   STRING_AGG(DISTINCT c.mapped_from_id, ',') AS mapped_from
FROM (
    SELECT
        c.CONCEPT_ID, CONCEPT_NAME, COALESCE(c.STANDARD_CONCEPT, 'N') as STANDARD_CONCEPT, COALESCE(c.INVALID_REASON, 'V') as INVALID_REASON,
        c.CONCEPT_CODE, c.CONCEPT_CLASS_ID, c.DOMAIN_ID, c.VOCABULARY_ID, c.VALID_START_DATE, c.VALID_END_DATE,
        r.RELATIONSHIP_NAME, 1 as RELATIONSHIP_DISTANCE,
        CAST(cr.CONCEPT_ID_1 AS VARCHAR) as mapped_from_id
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

    UNION ALL

    SELECT
        ca.ANCESTOR_CONCEPT_ID as CONCEPT_ID, c.CONCEPT_NAME, COALESCE(c.STANDARD_CONCEPT, 'N') as STANDARD_CONCEPT, COALESCE(c.INVALID_REASON, 'V') as INVALID_REASON,
        c.CONCEPT_CODE, c.CONCEPT_CLASS_ID, c.DOMAIN_ID, c.VOCABULARY_ID, c.VALID_START_DATE, c.VALID_END_DATE,
        'Has ancestor of' as RELATIONSHIP_NAME, MIN_LEVELS_OF_SEPARATION as RELATIONSHIP_DISTANCE,
        CAST(ca.DESCENDANT_CONCEPT_ID AS VARCHAR) as mapped_from_id
    FROM
        @CDM_schema.concept_ancestor ca
    JOIN
        @CDM_schema.concept c ON c.CONCEPT_ID = ca.ANCESTOR_CONCEPT_ID
    WHERE
        ca.DESCENDANT_CONCEPT_ID IN (@conceptIdList)
        AND COALESCE(c.STANDARD_CONCEPT, 'N') IN ('S', 'C')
        AND ca.ANCESTOR_CONCEPT_ID NOT IN (@conceptIdList)

    UNION ALL

    SELECT
        ca.DESCENDANT_CONCEPT_ID as CONCEPT_ID, c.CONCEPT_NAME, COALESCE(c.STANDARD_CONCEPT, 'N') as STANDARD_CONCEPT, COALESCE(c.INVALID_REASON, 'V') as INVALID_REASON,
        c.CONCEPT_CODE, c.CONCEPT_CLASS_ID, c.DOMAIN_ID, c.VOCABULARY_ID, c.VALID_START_DATE, c.VALID_END_DATE,
        'Has descendant of' as RELATIONSHIP_NAME, MIN_LEVELS_OF_SEPARATION as RELATIONSHIP_DISTANCE,
        CAST(ca.ANCESTOR_CONCEPT_ID AS VARCHAR) as mapped_from_id
    FROM
        @CDM_schema.concept_ancestor ca
    JOIN
        @CDM_schema.concept c ON c.CONCEPT_ID = ca.DESCENDANT_CONCEPT_ID
    WHERE
        ca.ANCESTOR_CONCEPT_ID IN (@conceptIdList)
        AND COALESCE(c.STANDARD_CONCEPT, 'N') IN ('S', 'C')
        AND ca.DESCENDANT_CONCEPT_ID NOT IN (@conceptIdList)
) c
GROUP BY
   c.CONCEPT_ID,
   c.CONCEPT_NAME,
   c.STANDARD_CONCEPT,
   c.INVALID_REASON,
   c.CONCEPT_CODE,
   c.CONCEPT_CLASS_ID,
   c.DOMAIN_ID,
   c.VOCABULARY_ID,
   c.VALID_START_DATE,
   c.VALID_END_DATE,
   c.RELATIONSHIP_NAME,
   c.RELATIONSHIP_DISTANCE
ORDER BY
   c.RELATIONSHIP_DISTANCE ASC;
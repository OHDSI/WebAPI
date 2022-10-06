select distinct * from (
	select
		c.CONCEPT_ID, 
		CONCEPT_NAME, 
		ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, 
		ISNULL(c.INVALID_REASON,'V') INVALID_REASON, 
		CONCEPT_CODE, 
		CONCEPT_CLASS_ID, 
		DOMAIN_ID, 
		c.VOCABULARY_ID,
	    c.VALID_START_DATE,
	    c.VALID_END_DATE
		-- , RELATIONSHIP_NAME, 1 RELATIONSHIP_DISTANCE
    from @CDM_schema.concept_relationship cr
    join @CDM_schema.concept c on cr.CONCEPT_ID_2 = c.CONCEPT_ID
    join @CDM_schema.relationship r on cr.RELATIONSHIP_ID = r.RELATIONSHIP_ID
    where cr.CONCEPT_ID_1 IN (@conceptList) and cr.INVALID_REASON IS NULL
    union 
    select ANCESTOR_CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(c.INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, c.VOCABULARY_ID, c.VALID_START_DATE, c.VALID_END_DATE --, 'Has ancestor of' , MIN_LEVELS_OF_SEPARATION RELATIONSHIP_DISTANCE
    from @CDM_schema.concept_ancestor ca
    join @CDM_schema.concept c on c.CONCEPT_ID = ca.ANCESTOR_CONCEPT_ID
    where DESCENDANT_CONCEPT_ID IN (@conceptList)
    --and ANCESTOR_CONCEPT_ID NOT IN (@conceptList)
    union 
    select DESCENDANT_CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(c.INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, c.VOCABULARY_ID, c.VALID_START_DATE, c.VALID_END_DATE --, 'Has descendant of' , MIN_LEVELS_OF_SEPARATION RELATIONSHIP_DISTANCE
    from @CDM_schema.concept_ancestor ca
    join @CDM_schema.concept c on c.CONCEPT_ID = ca.DESCENDANT_CONCEPT_ID
    where ANCESTOR_CONCEPT_ID IN (@conceptList)
    --and DESCENDANT_CONCEPT_ID NOT IN (@conceptList)
    union
    select distinct c3.CONCEPT_ID, c3.CONCEPT_NAME,isnull(c3.standard_concept,'N') STANDARD_CONCEPT, ISNULL(c3.INVALID_REASON,'V') INVALID_REASON, c3.CONCEPT_CODE, c3.CONCEPT_CLASS_ID, c3.DOMAIN_ID, c3.VOCABULARY_ID, c3.VALID_START_DATE, c3.VALID_END_DATE --, RELATIONSHIP_NAME, min_levels_of_separation RELATIONSHIP_DISTANCE
    from (
            select * from @CDM_schema.concept where concept_id IN (@conceptList)
    ) c1
    join @CDM_schema.concept_ancestor ca1 on c1.concept_id = ca1.ancestor_concept_id
    join @CDM_schema.concept_relationship cr1 on ca1.descendant_concept_id = cr1.concept_id_1 and cr1.relationship_id = 'Mapped from'
    join @CDM_schema.relationship r on r.relationship_id = cr1.relationship_id
    join @CDM_schema.concept c3 on cr1.concept_id_2 = c3.concept_id
) ALL_RELATED 
WHERE 
@filters
ORDER BY CONCEPT_NAME ASC;

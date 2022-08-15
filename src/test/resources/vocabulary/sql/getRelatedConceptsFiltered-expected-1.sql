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
		
    from vocab_schema.concept_relationship cr
    join vocab_schema.concept c on cr.CONCEPT_ID_2 = c.CONCEPT_ID
    join vocab_schema.relationship r on cr.RELATIONSHIP_ID = r.RELATIONSHIP_ID
    where cr.CONCEPT_ID_1 IN (?,?,?,?,?) and cr.INVALID_REASON IS NULL
    union 
    select ANCESTOR_CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(c.INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, c.VOCABULARY_ID, c.VALID_START_DATE, c.VALID_END_DATE
    from vocab_schema.concept_ancestor ca
    join vocab_schema.concept c on c.CONCEPT_ID = ca.ANCESTOR_CONCEPT_ID
    where DESCENDANT_CONCEPT_ID IN (?,?,?,?,?)
    
    union 
    select DESCENDANT_CONCEPT_ID, CONCEPT_NAME, ISNULL(STANDARD_CONCEPT,'N') STANDARD_CONCEPT, ISNULL(c.INVALID_REASON,'V') INVALID_REASON, CONCEPT_CODE, CONCEPT_CLASS_ID, DOMAIN_ID, c.VOCABULARY_ID, c.VALID_START_DATE, c.VALID_END_DATE
    from vocab_schema.concept_ancestor ca
    join vocab_schema.concept c on c.CONCEPT_ID = ca.DESCENDANT_CONCEPT_ID
    where ANCESTOR_CONCEPT_ID IN (?,?,?,?,?)
    
    union
    select distinct c3.CONCEPT_ID, c3.CONCEPT_NAME,isnull(c3.standard_concept,'N') STANDARD_CONCEPT, ISNULL(c3.INVALID_REASON,'V') INVALID_REASON, c3.CONCEPT_CODE, c3.CONCEPT_CLASS_ID, c3.DOMAIN_ID, c3.VOCABULARY_ID, c3.VALID_START_DATE, c3.VALID_END_DATE
    from (
            select * from vocab_schema.concept where concept_id IN (?,?,?,?,?)
    ) c1
    join vocab_schema.concept_ancestor ca1 on c1.concept_id = ca1.ancestor_concept_id
    join vocab_schema.concept_relationship cr1 on ca1.descendant_concept_id = cr1.concept_id_1 and cr1.relationship_id = 'Mapped from'
    join vocab_schema.relationship r on r.relationship_id = cr1.relationship_id
    join vocab_schema.concept c3 on cr1.concept_id_2 = c3.concept_id
) ALL_RELATED 
WHERE 
VOCABULARY_ID IN (?,?,?) AND CONCEPT_CLASS_ID IN (?,?,?)
ORDER BY CONCEPT_NAME ASC

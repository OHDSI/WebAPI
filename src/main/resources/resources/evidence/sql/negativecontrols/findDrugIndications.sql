SELECT DISTINCT DESCENDANT_CONCEPT_ID AS CONCEPT_ID
INTO #TEMP_COI
FROM @vocabulary.concept_ancestor WHERE ANCESTOR_CONCEPT_ID IN ( @conceptsOfInterest );

CREATE INDEX IDX_TEMP_COI ON #TEMP_COI (CONCEPT_ID);

{@outcomeOfInterest == 'condition'}?{
  SELECT DISTINCT c3.CONCEPT_ID
  INTO #NC_INDICATIONS
  FROM @vocabulary.concept c1
  	JOIN @vocabulary.CONCEPT_ANCESTOR ca1
  		ON ca1.ANCESTOR_CONCEPT_ID = c1.CONCEPT_ID
  	JOIN @vocabulary.concept c2
  		ON c2.CONCEPT_ID = ca1.DESCENDANT_CONCEPT_ID
  		AND UPPER(c2.DOMAIN_ID) = 'DRUG'
  	JOIN #TEMP_COI coi
  		ON coi.CONCEPT_ID = c2.CONCEPT_ID
  	JOIN @vocabulary.concept_relationship cr1
    		ON cr1.CONCEPT_ID_1 = c1.CONCEPT_ID
  	JOIN @vocabulary.concept c3 /*Map Indications to SNOMED*/
    		ON c3.CONCEPT_ID = cr1.CONCEPT_ID_2
    		AND UPPER(c3.DOMAIN_ID) = 'CONDITION'
    		AND UPPER(c3.STANDARD_CONCEPT) = 'S'
  WHERE UPPER(c1.VOCABULARY_ID) IN ( 'INDICATION', 'IND / CI' )
  AND c1.INVALID_REASON IS NULL;
}

{@outcomeOfInterest == 'drug'}?{
  SELECT DISTINCT c3.CONCEPT_ID
  INTO #NC_INDICATIONS
  FROM #TEMP_COI coi
  	JOIN @vocabulary.concept_relationship cr1
  		ON cr1.CONCEPT_ID_2 = coi.CONCEPT_ID
  	JOIN @vocabulary.concept c1
  		ON c1.CONCEPT_ID = cr1.CONCEPT_ID_1
  		AND UPPER(c1.VOCABULARY_ID) IN ( 'INDICATION', 'IND / CI' )
  		AND c1.INVALID_REASON IS NULL
  	JOIN @vocabulary.concept_relationship cr2
  		ON cr2.CONCEPT_ID_1 = c1.CONCEPT_ID
  	JOIN @vocabulary.concept_ancestor ca1
  		ON ca1.DESCENDANT_CONCEPT_ID = cr2.CONCEPT_ID_2
  	JOIN @vocabulary.CONCEPT c3
  		ON c3.CONCEPT_ID = ca1.ANCESTOR_CONCEPT_ID
  		AND UPPER(c3.CONCEPT_CLASS_ID)= 'INGREDIENT'
  		AND UPPER(c3.DOMAIN_ID) = 'DRUG'
  		AND c3.STANDARD_CONCEPT = 'S';
}

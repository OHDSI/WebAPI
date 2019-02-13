{@outcomeOfInterest == 'condition'}?{
  SELECT c.CONCEPT_ID, c.CONCEPT_NAME,
  	MIN(u.SORT_ORDER) AS SORT_ORDER
	INTO #NC_CONCEPT_UNIVERSE
  FROM @cem_schema.NC_LU_CONCEPT_UNIVERSE u
  	JOIN @vocabulary.concept c
  		ON c.CONCEPT_ID = u.CONDITION_CONCEPT_ID
  		AND LOWER(c.DOMAIN_ID) = LOWER('condition')
  WHERE DRUG_CONCEPT_ID IN (
  	@conceptsOfInterest
  )
  GROUP BY c.CONCEPT_ID, c.CONCEPT_NAME;
}
{@outcomeOfInterest == 'drug'}?{
  SELECT c.CONCEPT_ID, c.CONCEPT_NAME,
  	MIN(u.SORT_ORDER) AS SORT_ORDER
	INTO #NC_CONCEPT_UNIVERSE
  FROM @cem_schema.NC_LU_CONCEPT_UNIVERSE u
  	JOIN @vocabulary.concept c
  		ON c.CONCEPT_ID = u.DRUG_CONCEPT_ID
  		AND LOWER(c.DOMAIN_ID) = LOWER('drug')
  WHERE CONDITION_CONCEPT_ID IN (
  	@conceptsOfInterest
  )
  GROUP BY c.CONCEPT_ID, c.CONCEPT_NAME;
}

CREATE INDEX tmp_cu_concept_id ON #NC_CONCEPT_UNIVERSE(concept_id);
CREATE INDEX tmp_cu_so ON #NC_CONCEPT_UNIVERSE(sort_order);

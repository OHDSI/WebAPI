SELECT DISTINCT CONCEPT_ID
INTO @storeData
FROM (
  {@conceptSetId != '0'}?{
		@conceptSetExpression
  }:{
    SELECT c1.CONCEPT_ID
    FROM @vocabulary.CONCEPT c1
    WHERE CONCEPT_ID = 0
  }
) z;

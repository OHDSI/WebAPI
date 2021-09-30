/*OPTIMIZE = 1 HERE MEANS IT IS GOOD TO KEEP!*/

/* ===STEP 1=== */
WITH CTE_NOT_PREVELANT_CONCEPTS AS (
	SELECT OUTCOME_OF_INTEREST_CONCEPT_ID, OUTCOME_OF_INTEREST_CONCEPT_NAME, SORT_ORDER, USER_INCLUDED
	FROM #NC_SUMMARY
	WHERE SORT_ORDER > 100000000000000000
),
CTE_CONCEPTS AS (
	/*FIND ALL CANDIDATE NEGATIVE CONTROLS TO OPTIMIZE*/
	SELECT OUTCOME_OF_INTEREST_CONCEPT_ID, OUTCOME_OF_INTEREST_CONCEPT_NAME, SORT_ORDER, USER_INCLUDED
	FROM #NC_SUMMARY
	WHERE DESCENDANT_PMID_COUNT = 0
	AND EXACT_PMID_COUNT = 0
	AND PARENT_PMID_COUNT = 0
	AND ANCESTOR_PMID_COUNT = 0
	AND IND_CI = 0
	{@outcomeOfInterest=='condition'}?{AND TOO_BROAD = 0}
	AND DRUG_INDUCED = 0
	{@outcomeOfInterest=='condition'}?{AND PREGNANCY = 0}
	AND DESCENDANT_SPLICER = 0
	AND EXACT_SPLICER = 0
	AND PARENT_SPLICER = 0
	AND ANCESTOR_SPLICER = 0
	AND DESCENDANT_FAERS = 0
	AND EXACT_FAERS = 0
	AND PARENT_FAERS = 0
	AND ANCESTOR_FAERS = 0
	AND USER_EXCLUDED = 0
),
/* ************************OPTIMIZATION LOGIC*********************************** */
conceptSetConcepts AS (
	-- Concepts that are part of the concept set definition that are "EXCLUDED = N, DECENDANTS = Y or N"
	select DISTINCT concept_id
	from @vocabulary.CONCEPT
	where concept_id in (
		SELECT OUTCOME_OF_INTEREST_CONCEPT_ID FROM CTE_CONCEPTS
	)
	and invalid_reason is null
),
conceptSetDescendants AS (
	select distinct ancestor_concept_id, descendant_concept_id concept_id
	from @vocabulary.concept_ancestor
	where ancestor_concept_id in (
		/*OUR LIST OF NEGATIVE CONTROLS*/
		SELECT OUTCOME_OF_INTEREST_CONCEPT_ID FROM CTE_CONCEPTS
	)
	and ancestor_concept_id != descendant_concept_id -- Exclude the selected ancestor itself
),
conceptSetAnalysis AS (
	SELECT
		a.concept_id original_concept_id
		, c1.concept_name original_concept_name
		, b.ancestor_concept_id
		, c3.concept_name ancestor_concept_name
		, b.concept_id subsumed_concept_id
		, c2.concept_name subsumed_concept_name
	FROM conceptSetConcepts a
		LEFT JOIN conceptSetDescendants b ON a.concept_id = b.concept_id
		LEFT JOIN @vocabulary.concept c1 ON a.concept_id = c1.concept_id
		LEFT JOIN @vocabulary.concept c2 ON b.concept_id = c2.concept_id
		LEFT JOIN @vocabulary.concept c3 ON b.ancestor_concept_id = c3.concept_id
),
conceptSetOptimized AS (
	SELECT original_concept_id concept_id
		, original_concept_name concept_name
	FROM conceptSetAnalysis
	WHERE subsumed_concept_id is null
),
conceptSetRemoved AS (
	SELECT DISTINCT subsumed_concept_id concept_id
		, subsumed_concept_name concept_name
	FROM conceptSetAnalysis
	WHERE subsumed_concept_id is not null
)
SELECT *
INTO #TEMP_NC_SUMMARY_OPT
FROM (
  SELECT *,
  CASE WHEN c.OUTCOME_OF_INTEREST_CONCEPT_ID IN (	SELECT CONCEPT_ID	FROM conceptSetOptimized ) THEN 1 ELSE 0 END AS OPTIMIZED,
  0 AS NOT_PREVELANT
  FROM CTE_CONCEPTS c
  UNION ALL
  SELECT *,
    0 AS OPTIMIZED,
    1 AS NOT_PREVELANT
  FROM CTE_NOT_PREVELANT_CONCEPTS
) z;

/* ===STEP 2=== */
/*ELIMINATE SIBLINGS*/
WITH CTE_LIST AS (
    /*Using the rows that were okay from above, let's further filter siblings*/
  	SELECT *, OUTCOME_OF_INTEREST_CONCEPT_ID AS CONCEPT_ID, OUTCOME_OF_INTEREST_CONCEPT_NAME AS CONCEPT_NAME
  	FROM #TEMP_NC_SUMMARY_OPT
  	WHERE OPTIMIZED = 1
    AND NOT_PREVELANT = 0
  ),
  CTE_PARENTS AS (
    /*Who are your parents?*/
  	SELECT DISTINCT l.*, c.CONCEPT_ID AS PARENT_CONCEPT_ID, c.CONCEPT_NAME AS PARENT_CONCEPT_NAME
  	FROM CTE_LIST l
  		LEFT OUTER JOIN @vocabulary.CONCEPT_RELATIONSHIP cr
  			ON cr.CONCEPT_ID_2 = l.CONCEPT_ID
  		LEFT OUTER JOIN @vocabulary.CONCEPT c
  			ON c.CONCEPT_ID = cr.CONCEPT_ID_1
  			AND c.STANDARD_CONCEPT = 'S'
  			AND c.CONCEPT_ID != l.CONCEPT_ID
  ),
  CTE_POSSIBLE_DUPLICATES AS (
    /*Parent has more than one child*/
  	SELECT PARENT_CONCEPT_ID, PARENT_CONCEPT_NAME, COUNT(*) AS ROW_COUNT
  	FROM CTE_PARENTS
  	GROUP BY PARENT_CONCEPT_ID, PARENT_CONCEPT_NAME
  	HAVING COUNT(*) > 1
  ),
  CTE_SELECTING_DUPLICATES AS (
    /*When a parent has more than one child, pick one*/
  	SELECT CONCEPT_ID, CONCEPT_NAME, SORT_ORDER, PARENT_CONCEPT_ID, PARENT_CONCEPT_NAME,
  		ROW_NUMBER() OVER(PARTITION BY PARENT_CONCEPT_ID ORDER BY PARENT_CONCEPT_ID, SORT_ORDER, CONCEPT_ID) AS ROW_NUM
  	FROM CTE_PARENTS
  	WHERE PARENT_CONCEPT_ID IN (
  		SELECT PARENT_CONCEPT_ID
  		FROM CTE_POSSIBLE_DUPLICATES
  	)
  ),
  CTE_KEEP_CONTROL AS (
	  SELECT l.CONCEPT_ID, l.CONCEPT_NAME, l.SORT_ORDER,
		MAX(d.ROW_NUM) AS ROW_NUM
	  FROM CTE_LIST l
  		JOIN CTE_SELECTING_DUPLICATES d
			  ON d.CONCEPT_ID = l.CONCEPT_ID
	  GROUP BY l.CONCEPT_ID, l.CONCEPT_NAME, l.SORT_ORDER
  )
  SELECT o.OUTCOME_OF_INTEREST_CONCEPT_ID,
  	o.OUTCOME_OF_INTEREST_CONCEPT_NAME,
	  o.SORT_ORDER,
	  o.USER_INCLUDED,
  	CASE
  		WHEN c.ROW_NUM IS NULL THEN o.OPTIMIZED
  		WHEN c.ROW_NUM = 1 THEN 1
  		WHEN c.ROW_NUM > 1 THEN 0
  		ELSE o.OPTIMIZED
  	END AS OPTIMIZED,
	  o.NOT_PREVELANT
INTO #NC_SUMMARY_OPTIMIZED
FROM #TEMP_NC_SUMMARY_OPT o
  	LEFT OUTER JOIN CTE_KEEP_CONTROL c
		  ON c.CONCEPT_ID = OUTCOME_OF_INTEREST_CONCEPT_ID;
--When OUTCOME of Interest = "DRUGS" you could perform this for each item
WITH CTE_DRUGS AS (
	SELECT DISTINCT c2.CONCEPT_ID, c2.CONCEPT_NAME
	FROM @vocabularySchema.concept c
		JOIN @vocabularySchema.concept_ancestor ca
			ON ca.DESCENDANT_CONCEPT_ID = c.CONCEPT_ID
			AND ca.ANCESTOR_CONCEPT_ID IN (
				--USER DEFINED DRUGS
				@conceptIds
			)
		JOIN @vocabularySchema.concept c2
			ON c2.CONCEPT_ID = ca.DESCENDANT_CONCEPT_ID
			WHERE c2.CONCEPT_CLASS_ID = 'Ingredient'
), 
CTE_LABELS_FOUND AS (
	SELECT DISTINCT c.CONCEPT_ID, c.CONCEPT_NAME
	FROM @cem_schema.CEM_UNIFIED s
		JOIN @vocabularySchema.concept_ancestor ca
			ON ca.DESCENDANT_CONCEPT_ID = s.CONCEPT_ID_1
		JOIN @vocabularySchema.concept c
			ON c.CONCEPT_ID = ca.ANCESTOR_CONCEPT_ID
			AND c.CONCEPT_CLASS_ID = 'Ingredient'
			AND c.CONCEPT_ID IN (
				SELECT CONCEPT_ID FROM CTE_DRUGS
			)
	WHERE s.SOURCE_ID = 'splicer' 
)
SELECT d.CONCEPT_ID, d.CONCEPT_NAME,
	CASE WHEN f.CONCEPT_ID IS NULL THEN 0 ELSE 1 END US_SPL_LABEL
FROM CTE_DRUGS d
	LEFT OUTER JOIN CTE_LABELS_FOUND f
		ON f.CONCEPT_ID = d.CONCEPT_ID
;

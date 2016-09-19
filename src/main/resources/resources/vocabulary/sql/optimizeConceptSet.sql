WITH conceptSetConcepts AS (
	-- Concepts that are part of the concept set definition that are "EXCLUDED = N, DECENDANTS = Y or N"
	select DISTINCT concept_id 
        from @cdm_database_schema.CONCEPT 
        where concept_id in (@allConcepts)
          and invalid_reason is null
), conceptSetDescendants AS (
	select distinct ancestor_concept_id, descendant_concept_id concept_id
	from @cdm_database_schema.concept_ancestor
	where ancestor_concept_id in (@descendantConcepts)
	  and ancestor_concept_id != descendant_concept_id -- Exclude the selected ancestor itself
), conceptSetAnalysis AS (
	SELECT 
		a.concept_id original_concept_id
		, c1.concept_name original_concept_name
		, b.ancestor_concept_id
		, c3.concept_name ancestor_concept_name
		, b.concept_id subsumed_concept_id
		, c2.concept_name subsumed_concept_name
	FROM conceptSetConcepts a
	LEFT JOIN conceptSetDescendants b ON a.concept_id = b.concept_id
	LEFT JOIN @cdm_database_schema.concept c1 ON a.concept_id = c1.concept_id
	LEFT JOIN @cdm_database_schema.concept c2 ON b.concept_id = c2.concept_id
	LEFT JOIN @cdm_database_schema.concept c3 ON b.ancestor_concept_id = c3.concept_id
), conceptSetOptimized AS (
	SELECT 
		original_concept_id concept_id
		, original_concept_name concept_name
	FROM conceptSetAnalysis
	WHERE subsumed_concept_id is null
), conceptSetRemoved AS (
	SELECT DISTINCT 
		subsumed_concept_id concept_id
		, subsumed_concept_name concept_name
	FROM conceptSetAnalysis
	WHERE subsumed_concept_id is not null
)	
SELECT *, 0 removed
FROM conceptSetOptimized
UNION
SELECT *, 1 removed
FROM conceptSetRemoved
;
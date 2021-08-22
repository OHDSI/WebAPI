/***********************************************************/
/***** Populate the hierarchy lookup table per treemap *****/
/***********************************************************/
TRUNCATE TABLE @results_schema.concept_hierarchy;
/********** CONDITION/CONDITION_ERA **********/
INSERT INTO @results_schema.concept_hierarchy
	(concept_id, concept_name, treemap, concept_hierarchy_type, level1_concept_name, level2_concept_name, level3_concept_name, level4_concept_name)
SELECT
	snomed.concept_id,
  snomed.concept_name AS concept_name,
	CAST('Condition' AS VARCHAR(20)) AS treemap,
	CAST(NULL AS VARCHAR(20)) as concept_hierarchy_type,
	pt_to_hlt.pt_concept_name as level1_concept_name,
	hlt_to_hlgt.hlt_concept_name as level2_concept_name,
	hlgt_to_soc.hlgt_concept_name as level3_concept_name,
	soc.concept_name AS level4_concept_name
FROM (
	SELECT
		concept_id,
		concept_name
	FROM @vocab_schema.concept
	WHERE domain_id = 'Condition'
) snomed
LEFT JOIN (
	SELECT
		c1.concept_id      AS snomed_concept_id,
		max(c2.concept_id) AS pt_concept_id
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.domain_id = 'Condition'
		AND ca1.min_levels_of_separation = 1
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.vocabulary_id = 'MedDRA'
	GROUP BY c1.concept_id
) snomed_to_pt ON snomed.concept_id = snomed_to_pt.snomed_concept_id
LEFT JOIN (
	SELECT
		c1.concept_id      AS pt_concept_id,
		c1.concept_name    AS pt_concept_name,
		max(c2.concept_id) AS hlt_concept_id
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.vocabulary_id = 'MedDRA'
		AND ca1.min_levels_of_separation = 1
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.vocabulary_id = 'MedDRA'
	GROUP BY c1.concept_id, c1.concept_name
) pt_to_hlt ON snomed_to_pt.pt_concept_id = pt_to_hlt.pt_concept_id
LEFT JOIN (
	SELECT
		c1.concept_id      AS hlt_concept_id,
		c1.concept_name    AS hlt_concept_name,
		max(c2.concept_id) AS hlgt_concept_id
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.vocabulary_id = 'MedDRA'
		AND ca1.min_levels_of_separation = 1
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.vocabulary_id = 'MedDRA'
	GROUP BY c1.concept_id, c1.concept_name
) hlt_to_hlgt ON pt_to_hlt.hlt_concept_id = hlt_to_hlgt.hlt_concept_id
LEFT JOIN (
	SELECT
		c1.concept_id      AS hlgt_concept_id,
		c1.concept_name    AS hlgt_concept_name,
		max(c2.concept_id) AS soc_concept_id
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.vocabulary_id = 'MedDRA'
		AND ca1.min_levels_of_separation = 1
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.vocabulary_id = 'MedDRA'
	GROUP BY c1.concept_id, c1.concept_name
) hlgt_to_soc ON hlt_to_hlgt.hlgt_concept_id = hlgt_to_soc.hlgt_concept_id
LEFT JOIN @vocab_schema.concept soc ON hlgt_to_soc.soc_concept_id = soc.concept_id;

/********** DRUG **********/
INSERT INTO @results_schema.concept_hierarchy
	(concept_id, concept_name, treemap, concept_hierarchy_type, level1_concept_name, level2_concept_name, level3_concept_name, level4_concept_name)
SELECT
	rxnorm.concept_id,
	rxnorm.concept_name AS concept_name,
	CAST('Drug' AS VARCHAR(20)) AS treemap,
	CAST(NULL AS VARCHAR(20)) as concept_hierarchy_type,
	rxnorm.rxnorm_ingredient_concept_name as level1_concept_name,
	atc5_to_atc3.atc5_concept_name as level2_concept_name,
	atc3_to_atc1.atc3_concept_name as level3_concept_name,
	atc1.concept_name AS level4_concept_name
FROM (
	SELECT
		c1.concept_id,
		c1.concept_name,
		c2.concept_id   AS rxnorm_ingredient_concept_id,
		c2.concept_name AS RxNorm_ingredient_concept_name
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.domain_id = 'Drug'
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.domain_id = 'Drug'
		AND c2.concept_class_id = 'Ingredient'
) rxnorm
LEFT JOIN (
	SELECT
		c1.concept_id      AS rxnorm_ingredient_concept_id,
		max(c2.concept_id) AS atc5_concept_id
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.domain_id = 'Drug'
		AND c1.concept_class_id = 'Ingredient'
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.vocabulary_id = 'ATC'
		AND c2.concept_class_id = 'ATC 4th'
	GROUP BY c1.concept_id
) rxnorm_to_atc5 ON rxnorm.rxnorm_ingredient_concept_id = rxnorm_to_atc5.rxnorm_ingredient_concept_id
LEFT JOIN (
	SELECT
		c1.concept_id      AS atc5_concept_id,
		c1.concept_name    AS atc5_concept_name,
		max(c2.concept_id) AS atc3_concept_id
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.vocabulary_id = 'ATC'
		AND c1.concept_class_id = 'ATC 4th'
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.vocabulary_id = 'ATC'
		AND c2.concept_class_id = 'ATC 2nd'
	GROUP BY c1.concept_id, c1.concept_name
) atc5_to_atc3 ON rxnorm_to_atc5.atc5_concept_id = atc5_to_atc3.atc5_concept_id
LEFT JOIN (
	SELECT
		c1.concept_id      AS atc3_concept_id,
		c1.concept_name    AS atc3_concept_name,
		max(c2.concept_id) AS atc1_concept_id
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.vocabulary_id = 'ATC'
		AND c1.concept_class_id = 'ATC 2nd'
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.vocabulary_id = 'ATC'
		AND c2.concept_class_id = 'ATC 1st'
	GROUP BY c1.concept_id, c1.concept_name
) atc3_to_atc1 ON atc5_to_atc3.atc3_concept_id = atc3_to_atc1.atc3_concept_id
LEFT JOIN @vocab_schema.concept atc1 ON atc3_to_atc1.atc1_concept_id = atc1.concept_id;

/********** DRUG_ERA **********/
INSERT INTO @results_schema.concept_hierarchy
	(concept_id, concept_name, treemap, concept_hierarchy_type, level1_concept_name, level2_concept_name, level3_concept_name, level4_concept_name)
SELECT
	rxnorm.rxnorm_ingredient_concept_id as concept_id,
	rxnorm.rxnorm_ingredient_concept_name as concept_name,
	CAST('Drug Era' AS VARCHAR(20)) AS treemap,
	CAST(NULL AS VARCHAR(20)) as concept_hierarchy_type,
	atc5_to_atc3.atc5_concept_name as level1_concept_name,
	atc3_to_atc1.atc3_concept_name as level2_concept_name,
	atc1.concept_name AS level3_concept_name,
	CAST(NULL AS VARCHAR(255)) as level4_concept_name
FROM (
	SELECT
		c2.concept_id   AS rxnorm_ingredient_concept_id,
		c2.concept_name AS RxNorm_ingredient_concept_name
	FROM @vocab_schema.concept c2
	WHERE c2.domain_id = 'Drug'
		AND c2.concept_class_id = 'Ingredient'
) rxnorm
LEFT JOIN (
	SELECT
		c1.concept_id      AS rxnorm_ingredient_concept_id,
		max(c2.concept_id) AS atc5_concept_id
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.domain_id = 'Drug'
		AND c1.concept_class_id = 'Ingredient'
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.vocabulary_id = 'ATC'
		AND c2.concept_class_id = 'ATC 4th'
	GROUP BY c1.concept_id
) rxnorm_to_atc5 ON rxnorm.rxnorm_ingredient_concept_id = rxnorm_to_atc5.rxnorm_ingredient_concept_id
LEFT JOIN (
	SELECT
		c1.concept_id      AS atc5_concept_id,
		c1.concept_name    AS atc5_concept_name,
		max(c2.concept_id) AS atc3_concept_id
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.vocabulary_id = 'ATC'
		AND c1.concept_class_id = 'ATC 4th'
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.vocabulary_id = 'ATC'
		AND c2.concept_class_id = 'ATC 2nd'
	GROUP BY c1.concept_id, c1.concept_name
) atc5_to_atc3 ON rxnorm_to_atc5.atc5_concept_id = atc5_to_atc3.atc5_concept_id
LEFT JOIN (
	SELECT
		c1.concept_id      AS atc3_concept_id,
		c1.concept_name    AS atc3_concept_name,
		max(c2.concept_id) AS atc1_concept_id
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.concept_ancestor ca1 ON c1.concept_id = ca1.descendant_concept_id
		AND c1.vocabulary_id = 'ATC'
		AND c1.concept_class_id = 'ATC 2nd'
	INNER JOIN @vocab_schema.concept c2 ON ca1.ancestor_concept_id = c2.concept_id
		AND c2.vocabulary_id = 'ATC'
		AND c2.concept_class_id = 'ATC 1st'
	GROUP BY c1.concept_id, c1.concept_name
) atc3_to_atc1 ON atc5_to_atc3.atc3_concept_id = atc3_to_atc1.atc3_concept_id
LEFT JOIN @vocab_schema.concept atc1 ON atc3_to_atc1.atc1_concept_id = atc1.concept_id;

/********** MEASUREMENT **********/
INSERT INTO @results_schema.concept_hierarchy
	(concept_id, concept_name, treemap, concept_hierarchy_type, level1_concept_name, level2_concept_name, level3_concept_name, level4_concept_name)
SELECT
	m.concept_id,
	m.concept_name AS concept_name,
	CAST('Measurement' AS VARCHAR(20)) AS treemap,
	CAST(NULL AS VARCHAR(20)) as concept_hierarchy_type,
	CAST(max(c1.concept_name) AS VARCHAR(255)) AS level1_concept_name,
	CAST(max(c2.concept_name) AS VARCHAR(255)) AS level2_concept_name,
	CAST(max(c3.concept_name) AS VARCHAR(255)) AS level3_concept_name,
	CAST(NULL AS VARCHAR(255)) as level4_concept_name
FROM (
	SELECT DISTINCT
		concept_id,
		concept_name
	FROM @vocab_schema.concept c
	WHERE domain_id = 'Measurement'
) m
LEFT JOIN @vocab_schema.concept_ancestor ca1 ON M.concept_id = ca1.DESCENDANT_CONCEPT_ID AND ca1.min_levels_of_separation = 1
LEFT JOIN @vocab_schema.concept c1 ON ca1.ANCESTOR_CONCEPT_ID = c1.concept_id
LEFT JOIN @vocab_schema.concept_ancestor ca2 ON c1.concept_id = ca2.DESCENDANT_CONCEPT_ID AND ca2.min_levels_of_separation = 1
LEFT JOIN @vocab_schema.concept c2 ON ca2.ANCESTOR_CONCEPT_ID = c2.concept_id
LEFT JOIN @vocab_schema.concept_ancestor ca3 ON c2.concept_id = ca3.DESCENDANT_CONCEPT_ID AND ca3.min_levels_of_separation = 1
LEFT JOIN @vocab_schema.concept c3 ON ca3.ANCESTOR_CONCEPT_ID = c3.concept_id
GROUP BY M.concept_id, M.concept_name;

/********** OBSERVATION **********/
INSERT INTO @results_schema.concept_hierarchy
	(concept_id, concept_name, treemap, concept_hierarchy_type, level1_concept_name, level2_concept_name, level3_concept_name, level4_concept_name)
SELECT
	obs.concept_id,
	obs.concept_name AS concept_name,
	CAST('Observation' AS VARCHAR(20)) AS treemap,
	CAST(NULL AS VARCHAR(20)) as concept_hierarchy_type,
	CAST(max(c1.concept_name) AS VARCHAR(255)) AS level1_concept_name,
	CAST(max(c2.concept_name) AS VARCHAR(255)) AS level2_concept_name,
	CAST(max(c3.concept_name) AS VARCHAR(255)) AS level3_concept_name,
	CAST(NULL AS VARCHAR(255)) as level4_concept_name
FROM (
	SELECT
		concept_id,
		concept_name
	FROM @vocab_schema.concept
	WHERE domain_id = 'Observation'
) obs
LEFT JOIN @vocab_schema.concept_ancestor ca1 ON obs.concept_id = ca1.DESCENDANT_CONCEPT_ID AND ca1.min_levels_of_separation = 1
LEFT JOIN @vocab_schema.concept c1 ON ca1.ANCESTOR_CONCEPT_ID = c1.concept_id
LEFT JOIN @vocab_schema.concept_ancestor ca2 ON c1.concept_id = ca2.DESCENDANT_CONCEPT_ID AND ca2.min_levels_of_separation = 1
LEFT JOIN @vocab_schema.concept c2 ON ca2.ANCESTOR_CONCEPT_ID = c2.concept_id
LEFT JOIN @vocab_schema.concept_ancestor ca3 ON c2.concept_id = ca3.DESCENDANT_CONCEPT_ID AND ca3.min_levels_of_separation = 1
LEFT JOIN @vocab_schema.concept c3 ON ca3.ANCESTOR_CONCEPT_ID = c3.concept_id
GROUP BY obs.concept_id, obs.concept_name;

/********** PROCEDURE **********/
INSERT INTO @results_schema.concept_hierarchy
	(concept_id, concept_name, treemap, concept_hierarchy_type, level1_concept_name, level2_concept_name, level3_concept_name, level4_concept_name)
SELECT
	procs.concept_id,
	CAST(procs.proc_concept_name AS VARCHAR(400)) AS concept_name,
	CAST('Procedure' AS VARCHAR(20)) AS treemap,
	CAST(NULL AS VARCHAR(20)) as concept_hierarchy_type,
	CAST(max(proc_hierarchy.os3_concept_name) AS VARCHAR(255)) AS level1_concept_name,
	CAST(max(proc_hierarchy.os2_concept_name) AS VARCHAR(255)) AS level2_concept_name,
	CAST(max(proc_hierarchy.os1_concept_name) AS VARCHAR(255)) AS level3_concept_name,
	CAST(NULL AS VARCHAR(255)) as level4_concept_name
FROM
(
	SELECT
		c1.concept_id,
		CONCAT(v1.vocabulary_name, ' ', c1.concept_code, ': ', c1.concept_name) AS proc_concept_name
	FROM @vocab_schema.concept c1
	INNER JOIN @vocab_schema.vocabulary v1 ON c1.vocabulary_id = v1.vocabulary_id
	WHERE c1.domain_id = 'Procedure'
) procs
LEFT JOIN (
	SELECT
		ca0.DESCENDANT_CONCEPT_ID,
		max(ca0.ancestor_concept_id) AS ancestor_concept_id
	FROM @vocab_schema.concept_ancestor ca0
	INNER JOIN (
		SELECT DISTINCT c2.concept_id AS os3_concept_id
		FROM @vocab_schema.concept_ancestor ca1
		INNER JOIN @vocab_schema.concept c1 ON ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
		INNER JOIN @vocab_schema.concept_ancestor ca2 ON c1.concept_id = ca2.ANCESTOR_CONCEPT_ID
		INNER JOIN @vocab_schema.concept c2 ON ca2.DESCENDANT_CONCEPT_ID = c2.concept_id
		WHERE ca1.ancestor_concept_id = 4040390
			AND ca1.Min_LEVELS_OF_SEPARATION = 2
			AND ca2.MIN_LEVELS_OF_SEPARATION = 1
	) t1 ON ca0.ANCESTOR_CONCEPT_ID = t1.os3_concept_id
	GROUP BY ca0.descendant_concept_id
) ca1 ON procs.concept_id = ca1.DESCENDANT_CONCEPT_ID
LEFT JOIN (
	SELECT
		proc_by_os1.os1_concept_name,
		proc_by_os2.os2_concept_name,
		proc_by_os3.os3_concept_name,
		proc_by_os3.os3_concept_id
	FROM (
		SELECT
			DESCENDANT_CONCEPT_ID AS os1_concept_id,
			concept_name          AS os1_concept_name
		FROM @vocab_schema.concept_ancestor ca1
		INNER JOIN @vocab_schema.concept c1 ON ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
		WHERE ancestor_concept_id = 4040390
			AND Min_LEVELS_OF_SEPARATION = 1
	) proc_by_os1
	INNER JOIN (
		SELECT
			max(c1.CONCEPT_ID) AS os1_concept_id,
			c2.concept_id      AS os2_concept_id,
			c2.concept_name    AS os2_concept_name
		FROM @vocab_schema.concept_ancestor ca1
		INNER JOIN @vocab_schema.concept c1 ON ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
		INNER JOIN @vocab_schema.concept_ancestor ca2 ON c1.concept_id = ca2.ANCESTOR_CONCEPT_ID
		INNER JOIN @vocab_schema.concept c2 ON ca2.DESCENDANT_CONCEPT_ID = c2.concept_id
		WHERE ca1.ancestor_concept_id = 4040390
			AND ca1.Min_LEVELS_OF_SEPARATION = 1
			AND ca2.MIN_LEVELS_OF_SEPARATION = 1
		GROUP BY c2.concept_id, c2.concept_name
	) proc_by_os2 ON proc_by_os1.os1_concept_id = proc_by_os2.os1_concept_id
	INNER JOIN (
		SELECT
			max(c1.CONCEPT_ID) AS os2_concept_id,
			c2.concept_id      AS os3_concept_id,
			c2.concept_name    AS os3_concept_name
		FROM @vocab_schema.concept_ancestor ca1
		INNER JOIN @vocab_schema.concept c1 ON ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
		INNER JOIN @vocab_schema.concept_ancestor ca2 ON c1.concept_id = ca2.ANCESTOR_CONCEPT_ID
		INNER JOIN @vocab_schema.concept c2 ON ca2.DESCENDANT_CONCEPT_ID = c2.concept_id
		WHERE ca1.ancestor_concept_id = 4040390
			AND ca1.Min_LEVELS_OF_SEPARATION = 2
			AND ca2.MIN_LEVELS_OF_SEPARATION = 1
		GROUP BY c2.concept_id, c2.concept_name
	) proc_by_os3 ON proc_by_os2.os2_concept_id = proc_by_os3.os2_concept_id
) proc_hierarchy ON ca1.ancestor_concept_id = proc_hierarchy.os3_concept_id
GROUP BY procs.concept_id, procs.proc_concept_name;

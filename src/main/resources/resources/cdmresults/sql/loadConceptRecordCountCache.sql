WITH concepts AS (
    SELECT
      CAST(ancestor_concept_id AS VARCHAR)   ancestor_id,
      CAST(descendant_concept_id AS VARCHAR) descendant_id
    FROM @vocabularyTableQualifier.concept_ancestor ca
), counts AS (
SELECT stratum_1 concept_id, MAX (count_value) agg_count_value
FROM @resultTableQualifier.achilles_results
WHERE analysis_id IN (2, 4, 5, 201, 301, 401, 501, 505, 601, 701, 801, 901, 1001, 1201, 1801)
		/* analyses:
 			 Number of persons by gender
			 Number of persons by race
			 Number of persons by ethnicity
			 Number of visit occurrence records, by visit_concept_id
			 Number of providers by specialty concept_id
			 Number of condition occurrence records, by condition_concept_id
			 Number of records of death, by cause_concept_id
			 Number of death records, by death_type_concept_id
			 Number of procedure occurrence records, by procedure_concept_id
			 Number of drug exposure records, by drug_concept_id
			 Number of observation occurrence records, by observation_concept_id
			 Number of drug era records, by drug_concept_id
			 Number of condition era records, by condition_concept_id
			 Number of visits by place of service
			 Number of measurement occurrence records, by observation_concept_id
		*/
GROUP BY stratum_1
UNION
SELECT stratum_2 AS concept_id, SUM (count_value) AS agg_count_value
FROM @resultTableQualifier.achilles_results
WHERE analysis_id IN (405, 605, 705, 805, 807, 1805, 1807)
		/* analyses:
			 Number of condition occurrence records, by condition_concept_id by condition_type_concept_id
			 Number of procedure occurrence records, by procedure_concept_id by procedure_type_concept_id
			 Number of drug exposure records, by drug_concept_id by drug_type_concept_id
			 Number of observation occurrence records, by observation_concept_id by observation_type_concept_id
			 Number of observation occurrence records, by observation_concept_id and unit_concept_id
			 Number of observation occurrence records, by measurement_concept_id by measurement_type_concept_id
			 Number of measurement occurrence records, by measurement_concept_id and unit_concept_id
		    but this subquery only gets the type or unit concept_ids, i.e., stratum_2
		*/
GROUP BY stratum_2
)
SELECT
  concepts.ancestor_id               concept_id,
  isnull(max(c1.agg_count_value), 0) record_count,
  isnull(sum(c2.agg_count_value), 0) descendant_record_count
/*
	in this main query and in the second subquery above, use sum to aggregate all descendant record counts togather
	but for ancestor record counts, the same value will be repeated for each row of join, so use max to get a single copy of that value
*/
FROM concepts
  LEFT JOIN counts c1 ON concepts.ancestor_id = c1.concept_id
  LEFT JOIN counts c2 ON concepts.descendant_id = c2.concept_id
GROUP BY concepts.ancestor_id
HAVING isnull(max(c1.agg_count_value), 0) > 0 OR isnull(sum(c2.agg_count_value), 0) > 0
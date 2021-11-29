WITH concepts AS (
    SELECT
      CAST(ancestor_concept_id AS VARCHAR)   ancestor_id,
      CAST(descendant_concept_id AS VARCHAR) descendant_id
    FROM @vocabularyTableQualifier.concept_ancestor ca
    WHERE ancestor_concept_id >= @conceptIdentifierMin and ancestor_concept_id <= @conceptIdentifierMax
    UNION
    SELECT
      CAST(concept_id AS VARCHAR) ancestor_id,
      CAST(concept_id AS VARCHAR) descendant_id
    FROM @vocabularyTableQualifier.concept c
    WHERE concept_id >= @conceptIdentifierMin and concept_id <= @conceptIdentifierMax
), counts AS (
SELECT stratum_1 concept_id, MAX (count_value) agg_count_value
FROM @resultTableQualifier.achilles_results
WHERE analysis_id IN (2, 4, 5, 201, 225, 301, 325, 401, 425, 501, 505, 525, 601, 625, 701, 725, 801, 825, 
826, 827, 901, 1001, 1201, 1203, 1425, 1801, 1825, 1826, 1827, 2101, 2125, 2301)
		/* analyses:
 			 Number of persons by gender
			 Number of persons by race
			 Number of persons by ethnicity
			 Number of visit occurrence records, by visit_concept_id
			 Number of visit_occurrence records, by visit_source_concept_id
			 Number of providers by specialty concept_id
			 Number of provider records, by specialty_source_concept_id
			 Number of condition occurrence records, by condition_concept_id
			 Number of condition_occurrence records, by condition_source_concept_id
			 Number of records of death, by cause_concept_id
			 Number of death records, by death_type_concept_id
			 Number of death records, by cause_source_concept_id
			 Number of procedure occurrence records, by procedure_concept_id
			 Number of procedure_occurrence records, by procedure_source_concept_id
			 Number of drug exposure records, by drug_concept_id
			 Number of drug_exposure records, by drug_source_concept_id
			 Number of observation occurrence records, by observation_concept_id
			 Number of observation records, by observation_source_concept_id
			 Number of observation records, by value_as_concept_id
			 Number of observation records, by unit_concept_id
			 Number of drug era records, by drug_concept_id
			 Number of condition era records, by condition_concept_id
			 Number of visits by place of service
			 Number of visit_occurrence records, by discharge_to_concept_id
			 Number of payer_plan_period records, by payer_source_concept_id
			 Number of measurement occurrence records, by observation_concept_id
			 Number of measurement records, by measurement_source_concept_id
			 Number of measurement records, by value_as_concept_id
			 Number of measurement records, by unit_concept_id
			 Number of device exposure records, by device_concept_id
			 Number of device_exposure records, by device_source_concept_id
			 Number of location records, by region_concept_id
		*/
GROUP BY stratum_1
UNION
SELECT stratum_2 AS concept_id, SUM (count_value) AS agg_count_value
FROM @resultTableQualifier.achilles_results
WHERE analysis_id IN (405, 605, 705, 805, 807, 1805, 1807, 2105)
		/* analyses:
			 Number of condition occurrence records, by condition_concept_id by condition_type_concept_id
			 Number of procedure occurrence records, by procedure_concept_id by procedure_type_concept_id
			 Number of drug exposure records, by drug_concept_id by drug_type_concept_id
			 Number of observation occurrence records, by observation_concept_id by observation_type_concept_id
			 Number of observation occurrence records, by observation_concept_id and unit_concept_id
			 Number of observation occurrence records, by measurement_concept_id by measurement_type_concept_id
			 Number of measurement occurrence records, by measurement_concept_id and unit_concept_id
			 Number of device exposure records, by device_concept_id by device_type_concept_id
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

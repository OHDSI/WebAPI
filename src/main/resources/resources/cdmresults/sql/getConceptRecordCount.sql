with concepts as (
	select CAST(ancestor_concept_id as VARCHAR) ancestor_id, CAST(descendant_concept_id as VARCHAR) descendant_id 
	from @vocabularyTableQualifier.concept_ancestor ca 
	where ancestor_concept_id in (@conceptIdentifiers)
), counts as (
	select stratum_1 concept_id, max(count_value) agg_count_value
	from @resultTableQualifier.achilles_results
	where analysis_id in (2, 4, 5, 201, 301, 401, 501, 505, 601, 701, 801, 901, 1001,1201,1801)
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
	group by stratum_1
	union
	select stratum_2 as concept_id, sum(count_value) as agg_count_value
	from @resultTableQualifier.achilles_results
	where analysis_id in (405, 605, 705, 805, 807, 1805, 1807)
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
	group by stratum_2
)
select concepts.ancestor_id concept_id, isnull(max(c1.agg_count_value),0) record_count, isnull(sum(c2.agg_count_value),0) descendant_record_count
/*
	in this main query and in the second subquery above, use sum to aggregate all descendant record counts togather
	but for ancestor record counts, the same value will be repeated for each row of join, so use max to get a single copy of that value
*/
from concepts
left join counts c1 on concepts.ancestor_id = c1.concept_id
left join counts c2 on concepts.descendant_id = c2.concept_id
group by concepts.ancestor_id

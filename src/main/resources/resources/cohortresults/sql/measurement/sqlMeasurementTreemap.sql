select 	concept_hierarchy.concept_id,
	isNull(concept_hierarchy.level3_concept_name,'NA') 
	+ '||' + isNull(concept_hierarchy.level2_concept_name,'NA')
	+ '||' + isNull(concept_hierarchy.level1_concept_name,'NA')
	+ '||' + isNull(concept_hierarchy.concept_name, 'NA') as concept_path,
	hr1.count_value as num_persons, 
	1.0*hr1.count_value / denom.count_value as percent_persons,
	1.0*hr2.count_value / hr1.count_value as records_per_person
from (select * from @ohdsi_database_schema.heracles_results where analysis_id = 1300 and cohort_definition_id in (@cohortDefinitionId)) hr1
	inner join
	(select * from @ohdsi_database_schema.heracles_results where analysis_id = 1301 and cohort_definition_id in (@cohortDefinitionId)) hr2
	on hr1.stratum_1 = hr2.stratum_1
	inner join
	(
		select obs.concept_id, obs.concept_name, max(c1.concept_name) as level1_concept_name, max(c2.concept_name) as level2_concept_name, max(c3.concept_name) as level3_concept_name
		from
		(
		select concept_id, concept_name
		from @cdm_database_schema.concept
		where vocabulary_id = 'LOINC'
		) obs left join @cdm_database_schema.concept_ancestor ca1 on obs.concept_id = ca1.DESCENDANT_CONCEPT_ID and ca1.min_levels_of_separation = 1
		left join @cdm_database_schema.concept c1 on ca1.ANCESTOR_CONCEPT_ID = c1.concept_id
		left join @cdm_database_schema.concept_ancestor ca2 on c1.concept_id = ca2.DESCENDANT_CONCEPT_ID and ca2.min_levels_of_separation = 1
		left join @cdm_database_schema.concept c2 on ca2.ANCESTOR_CONCEPT_ID = c2.concept_id
		left join @cdm_database_schema.concept_ancestor ca3 on c2.concept_id = ca3.DESCENDANT_CONCEPT_ID and ca3.min_levels_of_separation = 1
		left join @cdm_database_schema.concept c3 on ca3.ANCESTOR_CONCEPT_ID = c3.concept_id
		group by obs.concept_id, obs.concept_name
	) concept_hierarchy on hr1.stratum_1 = CAST(concept_hierarchy.concept_id as VARCHAR),
	(select count_value from @ohdsi_database_schema.heracles_results where analysis_id = 1 and cohort_definition_id in (@cohortDefinitionId)) denom
order by hr1.count_value desc

select 	concept_hierarchy.concept_id,
	isNull(concept_hierarchy.level3_concept_name,'NA') 
	+ '||' + isNull(concept_hierarchy.level2_concept_name,'NA')
	+ '||' + isNull(concept_hierarchy.level1_concept_name,'NA')
	+ '||' + isNull(concept_hierarchy.concept_name, 'NA') as concept_path,
	ar1.count_value as num_persons, 
	1.0*ar1.count_value / denom.count_value as percent_persons,
	1.0*ar2.count_value / ar1.count_value as records_per_person
from (select * from @results_database_schema.ACHILLES_results where analysis_id = 800) ar1
	inner join
	(select * from @results_database_schema.ACHILLES_results where analysis_id = 801) ar2
	on ar1.stratum_1 = ar2.stratum_1
	inner join
	(
		select obs.concept_id, obs.concept_name, max(c1.concept_name) as level1_concept_name, max(c2.concept_name) as level2_concept_name, max(c3.concept_name) as level3_concept_name
		from
		(
			select concept_id, concept_name
			from @vocab_database_schema.concept
			where domain_id = 'Observation'
		) obs left join @vocab_database_schema.concept_ancestor ca1 on obs.concept_id = ca1.DESCENDANT_CONCEPT_ID and ca1.min_levels_of_separation = 1
		left join @vocab_database_schema.concept c1 on ca1.ANCESTOR_CONCEPT_ID = c1.concept_id
		left join @vocab_database_schema.concept_ancestor ca2 on c1.concept_id = ca2.DESCENDANT_CONCEPT_ID and ca2.min_levels_of_separation = 1
		left join @vocab_database_schema.concept c2 on ca2.ANCESTOR_CONCEPT_ID = c2.concept_id
		left join @vocab_database_schema.concept_ancestor ca3 on c2.concept_id = ca3.DESCENDANT_CONCEPT_ID and ca3.min_levels_of_separation = 1
		left join @vocab_database_schema.concept c3 on ca3.ANCESTOR_CONCEPT_ID = c3.concept_id
		group by obs.concept_id, obs.concept_name
	) concept_hierarchy on ar1.stratum_1 = CAST(concept_hierarchy.concept_id as VARCHAR),
	(select count_value from @results_database_schema.ACHILLES_results where analysis_id = 1) denom
order by ar1.count_value desc

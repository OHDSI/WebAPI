
/*
PROCEDURE_OCCURRENCE

--treemap of all conditions
--analysis_id: 1870
--size - prevalence of drug  
--color:  risk difference of prevalence before / after index
*/


select   concept_hierarchy.concept_id,
	isNull(concept_hierarchy.proc_concept_name,'NA') as concept_name,
	isNull(concept_hierarchy.level2_concept_name,'NA') as level2_concept_name,
	isNull(concept_hierarchy.level3_concept_name,'NA') as level3_concept_name,
	isNull(concept_hierarchy.level4_concept_name,'NA') as level4_concept_name,
	CONCAT(
	  isNull(concept_hierarchy.level4_concept_name,'NA'), '||',
	  isNull(concept_hierarchy.level3_concept_name,'NA'), '||',
	  isNull(concept_hierarchy.level2_concept_name,'NA'), '||',
	  isNull(concept_hierarchy.proc_concept_name,'NA')
	) as concept_path,
	1.0*hr1.num_persons / denom.count_value as percent_persons,
	1.0*hr1.num_persons_before / denom.count_value as percent_persons_before,
	1.0*hr1.num_persons_after / denom.count_value as percent_persons_after,
	1.0*(hr1.num_persons_after - hr1.num_persons_before)/denom.count_value as risk_diff_after_before,
	log(1.0*(hr1.num_persons_after + 0.5) / (hr1.num_persons_before + 0.5)) as logRR_after_before,
	hr1.num_persons,
        denom.count_value
from
(select stratum_1 as concept_id,
	sum(count_value) as num_persons,
	sum(case when CAST(stratum_2 AS INT) < 0 then count_value else 0 end) as num_persons_before,
	sum(case when CAST(stratum_2 AS INT) > 0 then count_value else 0 end) as num_persons_after
from @ohdsi_database_schema.heracles_results
where analysis_id in (1830) --first occurrence of procedure
and cohort_definition_id = @cohortDefinitionId
group by stratum_1
) hr1
inner join
	(
		select procs.concept_id,
		procs.proc_concept_name,
		max(proc_hierarchy.os3_concept_name) as level2_concept_name,
		max(proc_hierarchy.os2_concept_name) as level3_concept_name,
		max(proc_hierarchy.os1_concept_name) as level4_concept_name
	 from
		(
		select c1.concept_id, 
			CONCAT(v1.vocabulary_name, ' ', c1.concept_code, ': ', c1.concept_name) as proc_concept_name
		from @cdm_database_schema.concept c1
			inner join @cdm_database_schema.vocabulary v1
			on c1.vocabulary_id = v1.vocabulary_id
		where (
			c1.vocabulary_id in ('ICD9Proc', 'HCPCS','CPT4')
			or (c1.vocabulary_id = 'SNOMED' and c1.concept_class_id = 'Procedure')
			)
		) procs

	left join
		(select ca0.DESCENDANT_CONCEPT_ID, max(ca0.ancestor_concept_id) as ancestor_concept_id
		from @cdm_database_schema.concept_ancestor ca0
		inner join
		(select distinct c2.concept_id as os3_concept_id
		 from @cdm_database_schema.concept_ancestor ca1
			inner join
			@cdm_database_schema.concept c1
			on ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
			inner join
			@cdm_database_schema.concept_ancestor ca2
			on c1.concept_id = ca2.ANCESTOR_CONCEPT_ID
			inner join
			@cdm_database_schema.concept c2
			on ca2.DESCENDANT_CONCEPT_ID = c2.concept_id
		 where ca1.ancestor_concept_id = 4040390
		 and ca1.Min_LEVELS_OF_SEPARATION = 2
		 and ca2.MIN_LEVELS_OF_SEPARATION = 1
	  ) t1
	
		on ca0.ANCESTOR_CONCEPT_ID = t1.os3_concept_id

		group by ca0.descendant_concept_id

		) ca1
		on procs.concept_id = ca1.DESCENDANT_CONCEPT_ID
	left join
	(
	 select proc_by_os1.os1_concept_name,
		proc_by_os2.os2_concept_name,
		proc_by_os3.os3_concept_name,
		proc_by_os3.os3_concept_id
	from
	 (select DESCENDANT_CONCEPT_ID as os1_concept_id, concept_name as os1_concept_name
	 from @cdm_database_schema.concept_ancestor ca1
		inner join
		@cdm_database_schema.concept c1
		on ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
	 where ancestor_concept_id = 4040390
	 and Min_LEVELS_OF_SEPARATION = 1
	 ) proc_by_os1

	 inner join
	 (select max(c1.CONCEPT_ID) as os1_concept_id, c2.concept_id as os2_concept_id, c2.concept_name as os2_concept_name
	 from @cdm_database_schema.concept_ancestor ca1
		inner join
		@cdm_database_schema.concept c1
		on ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
		inner join
		@cdm_database_schema.concept_ancestor ca2
		on c1.concept_id = ca2.ANCESTOR_CONCEPT_ID
		inner join
		@cdm_database_schema.concept c2
		on ca2.DESCENDANT_CONCEPT_ID = c2.concept_id
	 where ca1.ancestor_concept_id = 4040390
	 and ca1.Min_LEVELS_OF_SEPARATION = 1
	 and ca2.MIN_LEVELS_OF_SEPARATION = 1
	 group by c2.concept_id, c2.concept_name
	 ) proc_by_os2
	 on proc_by_os1.os1_concept_id = proc_by_os2.os1_concept_id

	 inner join
	 (select max(c1.CONCEPT_ID) as os2_concept_id, c2.concept_id as os3_concept_id, c2.concept_name as os3_concept_name
	 from @cdm_database_schema.concept_ancestor ca1
		inner join
		@cdm_database_schema.concept c1
		on ca1.DESCENDANT_CONCEPT_ID = c1.concept_id
		inner join
		@cdm_database_schema.concept_ancestor ca2
		on c1.concept_id = ca2.ANCESTOR_CONCEPT_ID
		inner join
		@cdm_database_schema.concept c2
		on ca2.DESCENDANT_CONCEPT_ID = c2.concept_id
	 where ca1.ancestor_concept_id = 4040390
	 and ca1.Min_LEVELS_OF_SEPARATION = 2
	 and ca2.MIN_LEVELS_OF_SEPARATION = 1
	  group by c2.concept_id, c2.concept_name
	 ) proc_by_os3
	 on proc_by_os2.os2_concept_id = proc_by_os3.os2_concept_id
	) proc_hierarchy
	on ca1.ancestor_concept_id = proc_hierarchy.os3_concept_id
	group by procs.concept_id,
		procs.proc_concept_name

	) concept_hierarchy
	on cast(hr1.concept_id as integer)  = concept_hierarchy.concept_id
,
(select count_value
from @ohdsi_database_schema.heracles_results
where analysis_id = 1
and cohort_definition_id = @cohortDefinitionId
) denom
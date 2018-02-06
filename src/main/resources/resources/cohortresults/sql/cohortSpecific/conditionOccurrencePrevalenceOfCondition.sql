/*
CONDITION_OCCURRENCE

--treemap of all conditions
--analysis_id: 1820
--size - prevalence of condition  
--color:  risk difference of prevalence before / after index
*/
select   concept_hierarchy.concept_id,
	isNull(concept_hierarchy.soc_concept_name,'NA') as soc_concept_name,
	isNull(concept_hierarchy.hlgt_concept_name,'NA') as hlgt_concept_name,
	isNull(concept_hierarchy.hlt_concept_name,'NA') as hlt_concept_name,
	isNull(concept_hierarchy.pt_concept_name,'NA') as pt_concept_name,
	isNull(concept_hierarchy.snomed_concept_name,'NA') as concept_name,
  isNull(concept_hierarchy.soc_concept_name,'NA') + '||' + isNull(concept_hierarchy.hlgt_concept_name,'NA') + '||' + isNull(concept_hierarchy.hlt_concept_name,'NA') + '||' + isNull(concept_hierarchy.pt_concept_name,'NA') + '||' + isNull(concept_hierarchy.snomed_concept_name,'NA') as concept_path,
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
where analysis_id in (1820) --first occurrence of condition
and cohort_definition_id in (@cohortDefinitionId)
group by stratum_1
) hr1
inner join
	(
		select snomed.concept_id, 
			snomed.concept_name as snomed_concept_name,
			pt_to_hlt.pt_concept_name,
			hlt_to_hlgt.hlt_concept_name,
			hlgt_to_soc.hlgt_concept_name,
			soc.concept_name as soc_concept_name
		from	
		(
		select concept_id, concept_name
		from @cdm_database_schema.concept
		where vocabulary_id = 'SNOMED'
		) snomed
		left join
			(select c1.concept_id as snomed_concept_id, max(c2.concept_id) as pt_concept_id
			from
			@cdm_database_schema.concept c1
			inner join 
			@cdm_database_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'SNOMED'
			and ca1.min_levels_of_separation = 1
			inner join 
			@cdm_database_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'MedDRA'
			group by c1.concept_id
			) snomed_to_pt
		on snomed.concept_id = snomed_to_pt.snomed_concept_id

		left join
			(select c1.concept_id as pt_concept_id, c1.concept_name as pt_concept_name, max(c2.concept_id) as hlt_concept_id
			from
			@cdm_database_schema.concept c1
			inner join 
			@cdm_database_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'MedDRA'
			and ca1.min_levels_of_separation = 1
			inner join 
		  @cdm_database_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'MedDRA'
			group by c1.concept_id, c1.concept_name
			) pt_to_hlt
		on snomed_to_pt.pt_concept_id = pt_to_hlt.pt_concept_id

		left join
			(select c1.concept_id as hlt_concept_id, c1.concept_name as hlt_concept_name, max(c2.concept_id) as hlgt_concept_id
			from
			@cdm_database_schema.concept c1
			inner join 
			@cdm_database_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'MedDRA'
			and ca1.min_levels_of_separation = 1
			inner join 
			@cdm_database_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'MedDRA'
			group by c1.concept_id, c1.concept_name
			) hlt_to_hlgt
		on pt_to_hlt.hlt_concept_id = hlt_to_hlgt.hlt_concept_id

		left join
			(select c1.concept_id as hlgt_concept_id, c1.concept_name as hlgt_concept_name, max(c2.concept_id) as soc_concept_id
			from
			@cdm_database_schema.concept c1
			inner join 
			@cdm_database_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'MedDRA'
			and ca1.min_levels_of_separation = 1
			inner join 
			@cdm_database_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'MedDRA'
			group by c1.concept_id, c1.concept_name
			) hlgt_to_soc
		on hlt_to_hlgt.hlgt_concept_id = hlgt_to_soc.hlgt_concept_id

		left join @cdm_database_schema.concept soc
		 on hlgt_to_soc.soc_concept_id = soc.concept_id

	) concept_hierarchy
	on cast(hr1.concept_id as integer) = concept_hierarchy.concept_id
,
(select count_value
from @ohdsi_database_schema.heracles_results
where analysis_id = 1
and cohort_definition_id in (@cohortDefinitionId)
) denom
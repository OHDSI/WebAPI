

/*
DRUG_ERA

--treemap of all conditions
--analysis_id: 1870
--size - prevalence of drug  
--color:  risk difference of prevalence before / after index
*/


select   concept_hierarchy.concept_id,
	isnull(concept_hierarchy.rxnorm_ingredient_concept_name,'NA') as ingredient_concept_name,
	isnull(concept_hierarchy.atc5_concept_name,'NA') as atc5_concept_name,
	isnull(concept_hierarchy.atc3_concept_name,'NA') as atc3_concept_name,
	isnull(concept_hierarchy.atc1_concept_name,'NA') as atc1_concept_name,
  CONCAT(
    isnull(concept_hierarchy.atc1_concept_name,'NA'), '||',
    isnull(concept_hierarchy.atc3_concept_name,'NA'), '||',
    isnull(concept_hierarchy.atc5_concept_name,'NA'), '||',
    isnull(concept_hierarchy.rxnorm_ingredient_concept_name,'||')
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
	sum(case when CAST(CASE WHEN analysis_id = 1870 THEN stratum_2 ELSE null END AS INT) < 0 then count_value else 0 end) as num_persons_before,
	sum(case when CAST(CASE WHEN analysis_id = 1870 THEN stratum_2 ELSE null END AS INT) > 0 then count_value else 0 end) as num_persons_after
from @ohdsi_database_schema.heracles_results
where analysis_id = 1870 --first occurrence of drug
and cohort_definition_id = @cohortDefinitionId
group by stratum_1
) hr1
inner join
	(
  	select rxnorm.rxnorm_ingredient_concept_id as concept_id,
			rxnorm.rxnorm_ingredient_concept_name, 
			atc5_to_atc3.atc5_concept_name,
			atc3_to_atc1.atc3_concept_name,
			atc1.concept_name as atc1_concept_name
		from	
		(
		select c2.concept_id as rxnorm_ingredient_concept_id, 
			c2.concept_name as RxNorm_ingredient_concept_name
		from 
			@cdm_database_schema.concept c2
			where
			c2.vocabulary_id = 'RxNorm'
			and c2.concept_class_id = 'Ingredient'
		) rxnorm
		left join
			(select c1.concept_id as rxnorm_ingredient_concept_id, max(c2.concept_id) as atc5_concept_id
			from
			@cdm_database_schema.concept c1
			inner join 
			@cdm_database_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'RxNorm'
			and c1.concept_class_id = 'Ingredient'
			inner join 
			@cdm_database_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'ATC'
			and c2.concept_class_id = 'ATC 4th'
			group by c1.concept_id
			) rxnorm_to_atc5
		on rxnorm.rxnorm_ingredient_concept_id = rxnorm_to_atc5.rxnorm_ingredient_concept_id

		left join
			(select c1.concept_id as atc5_concept_id, c1.concept_name as atc5_concept_name, max(c2.concept_id) as atc3_concept_id
			from
			@cdm_database_schema.concept c1
			inner join 
			@cdm_database_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'ATC'
			and c1.concept_class_id = 'ATC 4th'
			inner join 
			@cdm_database_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'ATC'
			and c2.concept_class_id = 'ATC 2nd'
			group by c1.concept_id, c1.concept_name
			) atc5_to_atc3
		on rxnorm_to_atc5.atc5_concept_id = atc5_to_atc3.atc5_concept_id

		left join
			(select c1.concept_id as atc3_concept_id, c1.concept_name as atc3_concept_name, max(c2.concept_id) as atc1_concept_id
			from
			@cdm_database_schema.concept c1
			inner join 
			@cdm_database_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'ATC'
			and c1.concept_class_id = 'ATC 2nd'
			inner join 
			@cdm_database_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'ATC'
  		and c2.concept_class_id = 'ATC 1st'
			group by c1.concept_id, c1.concept_name
			) atc3_to_atc1
		on atc5_to_atc3.atc3_concept_id = atc3_to_atc1.atc3_concept_id

		left join @cdm_database_schema.concept atc1
		 on atc3_to_atc1.atc1_concept_id = atc1.concept_id
	) concept_hierarchy
	on cast(hr1.concept_id as integer) = concept_hierarchy.concept_id
,
(select count_value
from @ohdsi_database_schema.heracles_results
where analysis_id = 1
and cohort_definition_id = @cohortDefinitionId
) denom
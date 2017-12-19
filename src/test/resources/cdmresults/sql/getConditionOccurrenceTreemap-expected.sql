SELECT
  concept_hierarchy.concept_id,
  CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(
    isNull(concept_hierarchy.soc_concept_name, 'NA'), '||'),
    isNull(concept_hierarchy.hlgt_concept_name, 'NA')), '||'),
    isNull(concept_hierarchy.hlt_concept_name, 'NA')), '||'),
    isNull(concept_hierarchy.pt_concept_name, 'NA')), '||'),
    isNull(concept_hierarchy.snomed_concept_name, 'NA')) AS "conceptPath",
  hr1.count_value                                     AS num_persons,
  round(1.0 * hr1.count_value / denom.count_value, 5) AS percent_persons,
  round(1.0 * hr2.count_value / hr1.count_value, 5)   AS records_per_person
from (select * from result_schema.achilles_results where analysis_id = 400 ) hr1
	inner join
	(select * from result_schema.achilles_results where analysis_id = 401 ) hr2
	on hr1.stratum_1 = hr2.stratum_1
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
		from cdm_schema.concept
		where vocabulary_id = 'SNOMED'
		and concept_id in (?,?,?,?,?,?,?,?,?,?)
		) snomed
		left join
			(select c1.concept_id as snomed_concept_id, max(c2.concept_id) as pt_concept_id
			from
			cdm_schema.concept c1
			inner join 
			cdm_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'SNOMED'
			and ca1.min_levels_of_separation = 1
			inner join 
			cdm_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'MedDRA'
			group by c1.concept_id
			) snomed_to_pt
		on snomed.concept_id = snomed_to_pt.snomed_concept_id

		left join
			(select c1.concept_id as pt_concept_id, c1.concept_name as pt_concept_name, max(c2.concept_id) as hlt_concept_id
			from
			cdm_schema.concept c1
			inner join 
			cdm_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'MedDRA'
			and ca1.min_levels_of_separation = 1
			inner join 
		  cdm_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'MedDRA'
			group by c1.concept_id, c1.concept_name
			) pt_to_hlt
		on snomed_to_pt.pt_concept_id = pt_to_hlt.pt_concept_id

		left join
			(select c1.concept_id as hlt_concept_id, c1.concept_name as hlt_concept_name, max(c2.concept_id) as hlgt_concept_id
			from
			cdm_schema.concept c1
			inner join 
			cdm_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'MedDRA'
			and ca1.min_levels_of_separation = 1
			inner join 
			cdm_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'MedDRA'
			group by c1.concept_id, c1.concept_name
			) hlt_to_hlgt
		on pt_to_hlt.hlt_concept_id = hlt_to_hlgt.hlt_concept_id

		left join
			(select c1.concept_id as hlgt_concept_id, c1.concept_name as hlgt_concept_name, max(c2.concept_id) as soc_concept_id
			from
			cdm_schema.concept c1
			inner join 
			cdm_schema.concept_ancestor ca1
			on c1.concept_id = ca1.descendant_concept_id
			and c1.vocabulary_id = 'MedDRA'
			and ca1.min_levels_of_separation = 1
			inner join 
			cdm_schema.concept c2
			on ca1.ancestor_concept_id = c2.concept_id
			and c2.vocabulary_id = 'MedDRA'
			group by c1.concept_id, c1.concept_name
			) hlgt_to_soc
		on hlt_to_hlgt.hlgt_concept_id = hlgt_to_soc.hlgt_concept_id

		left join cdm_schema.concept soc
		 on hlgt_to_soc.soc_concept_id = soc.concept_id



	) concept_hierarchy
	on hr1.stratum_1 = CAST(concept_hierarchy.concept_id as VARCHAR(255))
	,
	(select count_value from result_schema.achilles_results where analysis_id = 1 ) denom

order by hr1.count_value desc

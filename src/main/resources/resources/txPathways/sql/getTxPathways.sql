WITH drug_ranks AS
(
select
first_start,
drug_concept_id,
concept_name,
subject_id,
rank() over (partition by subject_id order by first_start, drug_concept_id) as rank
from
(select
min(drug_era.drug_era_start_date) as first_start,
drug_era.drug_concept_id,
concept.concept_name,
cohort.subject_id
from
@tableQualifier2.cohort cohort
left join
@tableQualifier1.drug_era drug_era
on
(
cohort.subject_id = drug_era.person_id
)
left join
@tableQualifier1.concept concept
on
(
drug_era.drug_concept_id = concept.concept_id
)
where
cohort.cohort_definition_id = @cohortId

and
drug_era.drug_concept_id in (
select
drug_concept_id
from @tableQualifier2.concept_set_item
where concept_set_id=@conceptSetId

)
group by
drug_era.drug_concept_id,
concept.concept_name,
cohort.subject_id
) t
)
select
d1.subject_id,
d1.rank drug1_rank,
d1.drug_concept_id as drug1_concept_id,
d1.concept_name as drug1_concept_name,
d2.rank as drug2_rank,
d2.drug_concept_id as drug2_concept_id,
d2.concept_name as drug2_concept_name,
d3.rank as drug3_rank,
d3.drug_concept_id as drug3_concept_id,
d3.concept_name as drug3_concept_name,
d4.rank as drug4_rank,
d4.drug_concept_id as drug4_concept_id,
d4.concept_name as drug4_concept_name,
d5.rank as drug5_rank,
d5.drug_concept_id as drug5_concept_id,
d5.concept_name as drug5_concept_name,
d6.rank as drug6_rank,
d6.drug_concept_id as drug6_concept_id,
d6.concept_name as drug6_concept_name,
d7.rank as drug7_rank,
d7.drug_concept_id as drug7_concept_id,
d7.concept_name as drug7_concept_name,
count(d1.rank) as num_edges

from
drug_ranks d1
left join
drug_ranks d2
on
(
d1.rank = d2.rank - 1
and
d1.subject_id = d2.subject_id
)
left join
drug_ranks d3
on 
(
d1.rank = d3.rank - 2
and 
d1.subject_id = d3.subject_id
)
left join
drug_ranks d4
on 
(
d1.rank = d4.rank - 3
and 
d1.subject_id = d4.subject_id
)
left join
drug_ranks d5
on 
(
d1.rank = d5.rank - 4
and 
d1.subject_id = d5.subject_id
)
left join
drug_ranks d6
on 
(
d1.rank = d6.rank - 5
and 
d1.subject_id = d6.subject_id
)
left join
drug_ranks d7
on 
(
d1.rank = d7.rank - 6
and 
d1.subject_id = d7.subject_id
)
where d1.rank = 1
group by
d1.subject_id,
d1.rank,
d1.drug_concept_id,
d1.concept_name,
d2.rank,
d2.drug_concept_id,
d2.concept_name,
d3.rank,
d3.drug_concept_id,
d3.concept_name,
d4.rank,
d4.drug_concept_id,
d4.concept_name,
d5.rank,
d5.drug_concept_id,
d5.concept_name,
d6.rank,
d6.drug_concept_id,
d6.concept_name,
d7.rank,
d7.drug_concept_id,
d7.concept_name
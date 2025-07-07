select distinct ancestor_concept_id ancestor_id, descendant_concept_id descendant_id
from @CDM_schema.concept_ancestor
where ancestor_concept_id in (@ancestors)
UNION
select concept_id as ancestor_id, concept_id as descendant_id
from @CDM_schema.concept where concept_id in (@ancestors)

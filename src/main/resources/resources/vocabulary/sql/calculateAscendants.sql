select distinct ancestor_concept_id ancestor_id, descendant_concept_id descendant_id
from @CDM_schema.concept_ancestor
where ancestor_concept_id in (@ancestors) and descendant_concept_id in (@descendants)
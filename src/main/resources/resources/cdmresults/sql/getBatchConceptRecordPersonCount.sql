select cc.concept_id, record_count, descendant_record_count, person_count, descendant_person_count
from @resultTableQualifier.achilles_result_concept_count cc
join @vocabularyTableQualifier.concept c on cc.concept_id = c.concept_id
WHERE cc.concept_id >= @conceptIdentifierMin and cc.concept_id <= @conceptIdentifierMax

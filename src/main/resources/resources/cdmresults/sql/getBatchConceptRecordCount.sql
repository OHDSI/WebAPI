select cc.concept_id, record_count, descendant_record_count
from @resultTableQualifier.achilles_result_concept_count
join @vocabTableQualifier.concept c on cc.concept_id = c.concept_id
WHERE cc.concept_id >= @conceptIdentifierMin and cc.concept_id <= @conceptIdentifierMax

select concept_id, record_count, descendant_record_count
from @resultTableQualifier.achilles_result_concept_count
WHERE concept_id >= @conceptIdentifierMin and concept_id <= @conceptIdentifierMax

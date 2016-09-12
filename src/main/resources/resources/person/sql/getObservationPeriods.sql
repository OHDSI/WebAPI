select observation_period_id, observation_period_start_date [start_date], observation_period_end_date [end_date], concept_name observation_period_type
from @tableQualifier.observation_period op
join @tableQualifier.concept c on c.concept_id = op.period_type_concept_id
where person_id = @personId

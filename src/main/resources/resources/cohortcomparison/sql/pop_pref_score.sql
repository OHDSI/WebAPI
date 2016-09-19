select preference_score score, treatment, count(*) person_count
from @resultsTableQualifier.cca_pop 
where execution_id = @executionId
group by preference_score, treatment
order by preference_score asc
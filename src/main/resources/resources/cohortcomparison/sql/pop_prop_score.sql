select propensity_score score, treatment, count(*) person_count
from @resultsTableQualifier.cca_pop 
where execution_id = @executionId
group by propensity_score, treatment
order by propensity_score asc
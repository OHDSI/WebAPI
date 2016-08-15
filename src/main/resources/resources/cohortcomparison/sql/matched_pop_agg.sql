select round(propensity_score,2) ps, treatment, count(*) person_count
from @resultsTableQualifier.cca_matched_pop 
where execution_id = @executionId
group by round(propensity_score,2), treatment
order by round(propensity_score,2) asc
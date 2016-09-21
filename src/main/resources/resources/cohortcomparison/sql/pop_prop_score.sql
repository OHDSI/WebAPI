WITH C as (
	select propensity_score score, count(*) comparator
	from @resultsTableQualifier.cca_pop 
	where execution_id = @executionId and treatment = 0
	group by propensity_score, treatment
), 
T as (
	select propensity_score score, count(*) treatment
	from @resultsTableQualifier.cca_pop 
	where execution_id = @executionId and treatment = 1
	group by propensity_score, treatment
)
select T.score, ISNULL(treatment,0) treatment, ISNULL(comparator,0) comparator
from T left join C on T.score = C.score
order by T.score asc
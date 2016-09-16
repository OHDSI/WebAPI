select treatment_id, comparator_id, outcome_id, estimate, lower95, upper95, log_rr, se_log_rr
from @resultsTableQualifier.cca_om
where execution_id = @executionId
select event_id, person_id, @dateField
into #cohort_ends
from #included_events
@groupBy

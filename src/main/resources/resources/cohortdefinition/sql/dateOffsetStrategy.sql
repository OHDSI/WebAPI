select event_id, person_id, 
  case when DATEADD(day,@offset,@dateField) > start_date then DATEADD(day,@offset,@dateField) else start_date end as end_date
from @eventTable

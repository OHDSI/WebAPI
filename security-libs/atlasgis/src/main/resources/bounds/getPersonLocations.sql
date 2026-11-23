SELECT
  l.latitude,
  l.longitude,
  lh.start_date,
  lh.end_date
FROM @cdmSchema.location_history lh
	JOIN @cdmSchema.location l
	  ON lh.location_id = l.location_id
WHERE lh.domain_id = 'PERSON' AND lh.entity_id = @personId
{@startDate != ''} ? {AND ISNULL(end_date, '2099-10-25') >= '@startDate'}
{@endDate != ''} ? {AND ISNULL(start_date, '1900-01-01') <= '@endDate'}

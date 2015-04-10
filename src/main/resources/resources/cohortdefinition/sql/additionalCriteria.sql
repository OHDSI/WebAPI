SELECT p.person_id, P.start_date, P.end_date
FROM #PrimaryCriteriaEvents P
LEFT JOIN
(
  @criteriaQuery
) A on A.person_id = P.person_id and @windowCriteria
GROUP BY P.person_id, P.start_date, P.end_date
@occurrenceCriteria


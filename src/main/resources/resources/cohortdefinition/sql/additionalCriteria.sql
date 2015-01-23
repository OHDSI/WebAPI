SELECT p.PERSON_ID, P.START_DATE, P.END_DATE
FROM #PrimaryCriteriaEvents P
LEFT JOIN
(
  @criteriaQuery
) A on A.PERSON_ID = P.PERSON_ID
@windowCriteria
GROUP BY P.PERSON_ID, P.START_DATE, P.END_DATE
@occurrenceCriteria


/*
Based off the following paper:
Evans SJ, Waller PC, Davis S. Use of proportional reporting ratios (PRRs) for
signal generation from spontaneous adverse drug reaction reports.
Pharmacoepidemiol Drug Saf. 2001 Oct-Nov;10(6):483-6. PubMed PMID: 11828828.
*/


SELECT DISTINCT c.CONCEPT_ID, c.CONCEPT_NAME
INTO #NC_FAERS_CONCEPTS
FROM @translatedSchema.AEOLUS a
	JOIN @vocabulary.CONCEPT c
		{@outcomeOfInterest == 'condition'}?{ON c.CONCEPT_ID = a.CONCEPT_ID_2 WHERE CONCEPT_ID_1 IN }
		{@outcomeOfInterest == 'drug'}?{ON c.CONCEPT_ID = a.CONCEPT_ID_1 WHERE CONCEPT_ID_2 IN }
(
	SELECT CONCEPT_ID
	FROM @vocabulary.CONCEPT
	WHERE CONCEPT_ID IN (@conceptsOfInterest)
	UNION ALL
	SELECT DESCENDANT_CONCEPT_ID
	FROM @vocabulary.CONCEPT_ANCESTOR
	WHERE ANCESTOR_CONCEPT_ID IN (@conceptsOfInterest)
)
AND PRR >= 2
AND CASE_COUNT >= 3
AND CHI_SQUARE >= 4;

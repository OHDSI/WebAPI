select codeset_id, concept_id 
INTO #Codesets
FROM
(
 @codesetQueries
) C
;

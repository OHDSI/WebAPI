CREATE TABLE #Codesets
(
  CODESET_ID int not null,
  CONCEPT_ID int not null
)
;

INSERT INTO #Codesets
select CODESET_ID, CONCEPT_ID 
FROM
(
 @codesetQueries
) C
;

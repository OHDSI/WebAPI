CREATE TABLE #Codesets
(
  codeset_id int not null,
  concept_id int not null
)
;

INSERT INTO #Codesets
select codeset_id, concept_id 
FROM
(
 @codesetQueries
) C
;

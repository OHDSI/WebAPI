SELECT evidenceType,COUNT(*) as num FROM assertion1 where researchStatementLabel like "%example%" group by evidenceType


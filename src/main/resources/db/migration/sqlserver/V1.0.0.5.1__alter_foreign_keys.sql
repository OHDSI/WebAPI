IF (EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS 
                 WHERE CONSTRAINT_SCHEMA = 'dbo' 
                 AND  CONSTRAINT_NAME = 'FK_feasibility_inclusion_feasibility_study'))
BEGIN
ALTER TABLE dbo.feasibility_inclusion
  DROP CONSTRAINT FK_feasibility_inclusion_feasibility_study
END;

ALTER TABLE dbo.feasibility_inclusion 
  ADD CONSTRAINT FK_feasibility_inclusion_feasibility_study 
    FOREIGN KEY (study_id) 
    REFERENCES dbo.feasibility_study (id)
;

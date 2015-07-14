IF (EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS 
                 WHERE CONSTRAINT_SCHEMA = '${ohdsiSchema}' 
                 AND  CONSTRAINT_NAME = 'FK_feasibility_inclusion_feasibility_study'))
BEGIN
ALTER TABLE ${ohdsiSchema}.feasibility_inclusion
  DROP CONSTRAINT FK_feasibility_inclusion_feasibility_study
END;

ALTER TABLE ${ohdsiSchema}.feasibility_inclusion 
  ADD CONSTRAINT FK_feasibility_inclusion_feasibility_study 
    FOREIGN KEY (study_id) 
    REFERENCES ${ohdsiSchema}.feasibility_study (id)
;

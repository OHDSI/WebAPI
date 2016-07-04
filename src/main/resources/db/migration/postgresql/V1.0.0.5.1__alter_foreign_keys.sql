ALTER TABLE ${ohdsiSchema}.feasibility_inclusion
  DROP CONSTRAINT FK_feasibility_inclusion_feasibility_study
;

ALTER TABLE ${ohdsiSchema}.feasibility_inclusion 
  ADD CONSTRAINT FK_feasibility_inclusion_feasibility_study 
    FOREIGN KEY(study_id)
    REFERENCES ${ohdsiSchema}.feasibility_study (id)
;
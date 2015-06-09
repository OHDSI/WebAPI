ALTER TABLE feasibility_inclusion
  DROP CONSTRAINT FK_feasibility_inclusion_feasibility_study
;

ALTER TABLE feasibility_inclusion 
  ADD CONSTRAINT FK_feasibility_inclusion_feasibility_study 
    FOREIGN KEY(study_id)
    REFERENCES feasibility_study (id)
;
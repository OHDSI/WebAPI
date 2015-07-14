ALTER TABLE ${ohdsiSchema}.feasibility_inclusion
  DROP CONSTRAINT FK_fi_fs
;

ALTER TABLE ${ohdsiSchema}.feasibility_inclusion 
  ADD CONSTRAINT FK_fi_fs 
    FOREIGN KEY(study_id)
    REFERENCES ${ohdsiSchema}.feasibility_study (id)
;
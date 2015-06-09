ALTER TABLE feasibility_inclusion
  DROP CONSTRAINT FK_fi_fs
;

ALTER TABLE feasibility_inclusion 
  ADD CONSTRAINT FK_fi_fs 
    FOREIGN KEY(study_id)
    REFERENCES feasibility_study (id)
;
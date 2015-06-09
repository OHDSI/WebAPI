ALTER TABLE cohort_definition_details
  DROP CONSTRAINT FK_cdd_cd
;

ALTER TABLE cohort_definition_details 
  ADD CONSTRAINT FK_cdd_cd 
    FOREIGN KEY ( id) 
    REFERENCES cohort_definition (id)
;
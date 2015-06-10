ALTER TABLE cohort_definition_details
  DROP CONSTRAINT FK_cohort_definition_details_cohort_definition
;

ALTER TABLE cohort_definition_details 
  ADD CONSTRAINT FK_cohort_definition_details_cohort_definition 
    FOREIGN KEY (id) 
    REFERENCES cohort_definition (id)
;
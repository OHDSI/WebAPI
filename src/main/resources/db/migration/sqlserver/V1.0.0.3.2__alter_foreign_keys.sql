IF (EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS 
                 WHERE CONSTRAINT_SCHEMA = '${ohdsiSchema}' 
                 AND  CONSTRAINT_NAME = 'FK_cohort_definition_details_cohort_definition'))
BEGIN
ALTER TABLE ${ohdsiSchema}.cohort_definition_details
  DROP CONSTRAINT FK_cohort_definition_details_cohort_definition
END;

ALTER TABLE ${ohdsiSchema}.cohort_definition_details 
  ADD CONSTRAINT FK_cohort_definition_details_cohort_definition 
    FOREIGN KEY ( id) 
    REFERENCES ${ohdsiSchema}.cohort_definition (id)
;

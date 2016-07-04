ALTER TABLE ${ohdsiSchema}.cohort_definition_details
  DROP CONSTRAINT FK_cdd_cd
;

ALTER TABLE ${ohdsiSchema}.cohort_definition_details 
  ADD CONSTRAINT FK_cdd_cd 
    FOREIGN KEY ( id) 
    REFERENCES ${ohdsiSchema}.cohort_definition (id)
      ON DELETE CASCADE
;

CREATE TABLE ${ohdsiSchema}.cohort_generation_info(
  id Number(10) NOT NULL,
  start_time Timestamp(3) NOT NULL,
  execution_duration Number(10) NULL,
  status Number(10) NOT NULL,
  is_valid Number(1) NOT NULL,
  CONSTRAINT PK_cohort_generation_info PRIMARY KEY ( id),
  CONSTRAINT FK_cgi_cd FOREIGN KEY(id)
    REFERENCES ${ohdsiSchema}.cohort_definition (id)
    ON DELETE CASCADE
);

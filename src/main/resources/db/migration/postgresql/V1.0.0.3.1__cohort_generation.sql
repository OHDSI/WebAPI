ALTER TABLE ${ohdsiSchema}.cohort_definition_details
  DROP CONSTRAINT FK_cohort_definition_details_cohort_definition
;

ALTER TABLE ${ohdsiSchema}.cohort_definition_details 
  ADD CONSTRAINT FK_cohort_definition_details_cohort_definition 
    FOREIGN KEY (id) 
    REFERENCES ${ohdsiSchema}.cohort_definition (id)
      ON DELETE CASCADE
;

CREATE TABLE ${ohdsiSchema}.cohort_generation_info(
  id int NOT NULL,
  start_time Timestamp(3) NOT NULL,
  execution_duration int NULL,
  status int NOT NULL,
  is_valid Boolean NOT NULL,
  CONSTRAINT PK_cohort_generation_info PRIMARY KEY (id),
  CONSTRAINT FK_cohort_generation_info_cohort_definition FOREIGN KEY(id)
    REFERENCES ${ohdsiSchema}.cohort_definition (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

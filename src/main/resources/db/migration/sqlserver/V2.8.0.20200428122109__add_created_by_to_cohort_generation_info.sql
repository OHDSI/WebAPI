ALTER TABLE ${ohdsiSchema}.cohort_generation_info
  ADD created_by_id INT NULL, CONSTRAINT FK_cohort_def_su_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);
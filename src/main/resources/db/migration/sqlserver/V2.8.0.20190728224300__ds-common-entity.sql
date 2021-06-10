ALTER TABLE ${ohdsiSchema}.source
  ADD created_by_id INT NULL, CONSTRAINT FK_source_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

ALTER TABLE ${ohdsiSchema}.source
  ADD created_date DATETIME;

ALTER TABLE ${ohdsiSchema}.source
  ADD modified_by_id INT NULL, CONSTRAINT FK_source_mid FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

ALTER TABLE ${ohdsiSchema}.source
  ADD modified_date DATETIME;
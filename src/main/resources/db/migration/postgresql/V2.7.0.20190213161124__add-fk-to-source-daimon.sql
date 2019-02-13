ALTER TABLE ${ohdsiSchema}.source_daimon 
  ADD CONSTRAINT FK_source_daimon_source_id FOREIGN KEY (source_id) REFERENCES ${ohdsiSchema}.source (source_id);
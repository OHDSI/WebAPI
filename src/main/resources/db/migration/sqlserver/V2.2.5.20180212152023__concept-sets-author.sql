ALTER TABLE ${ohdsiSchema}.concept_set
  ADD created_by VARCHAR(255),
      modified_by VARCHAR(255),
      created_date DATETIMEOFFSET,
      modified_date DATETIMEOFFSET;
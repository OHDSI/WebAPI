ALTER TABLE ${ohdsiSchema}.concept_set
  ADD (
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_date TIMESTAMP WITH TIME ZONE,
    modified_date TIMESTAMP WITH TIME ZONE
  );
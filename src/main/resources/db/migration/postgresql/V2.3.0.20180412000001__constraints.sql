-- Add PK to cca table
ALTER TABLE ${ohdsiSchema}.cca ADD CONSTRAINT PK_cca_cca_id PRIMARY KEY (cca_id);

-- Add PK to concept_set_item
ALTER TABLE ${ohdsiSchema}.concept_set_item ADD CONSTRAINT PK_concept_set_item PRIMARY KEY (concept_set_item_id);

-- Add PK to plp table
ALTER TABLE ${ohdsiSchema}.plp ADD CONSTRAINT PK_plp_plp_id PRIMARY KEY (plp_id);

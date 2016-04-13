CREATE TABLE ${ohdsiSchema}.penelope_laertes_universe 
(
  id bigint,
  condition_concept_id integer,
  condition_concept_name character varying(255),
  ingredient_concept_id integer,
  ingredient_concept_name character varying(255),
  evidence_type character varying(255),
  supports character(1),
  statistic_value numeric,
  evidence_linkouts text
);

CREATE TABLE ${ohdsiSchema}.penelope_laertes_uni_pivot(
  condition_concept_id integer,
  condition_concept_name character varying(255),
  ingredient_concept_id integer,
  ingredient_concept_name character varying(255),
  medline_ct integer,
  medline_case integer,
  medline_other integer,
  semmeddb_ct_t integer,
  semmeddb_case_t integer,
  semmeddb_other_t integer,
  semmeddb_ct_f integer,
  semmeddb_case_f integer,
  semmeddb_other_f integer,
  eu_spc integer,
  spl_adr integer,
  aers integer,
  aers_prr numeric,
  aers_prr_original numeric
);

CREATE INDEX idx_penelope_laertes_uni_pivot
  ON penelope_laertes_uni_pivot
  USING btree
  (ingredient_concept_id, condition_concept_id);
ALTER TABLE penelope_laertes_uni_pivot CLUSTER ON idx_penelope_laertes_uni_pivot;
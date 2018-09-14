CREATE SEQUENCE ${ohdsiSchema}.pathway_analysis_sequence;
CREATE TABLE ${ohdsiSchema}.pathway_analysis
(
  id                 INTEGER NOT NULL,
  name               VARCHAR(255) NOT NULL,
  combination_window INTEGER,
  min_cell_count     INTEGER,
  max_depth          INTEGER,
  created_by_id      INTEGER,
  created_date       datetime,
  modified_by_id     INTEGER,
  modified_date      datetime,
  hash_code          INTEGER,
	CONSTRAINT [PK_pathway_analysis] PRIMARY KEY (id)
);

CREATE SEQUENCE ${ohdsiSchema}.pathway_cohort_sequence;

CREATE TABLE ${ohdsiSchema}.pathway_target_cohort
(
  id                   INTEGER NOT NULL,
  name                 VARCHAR(255) NOT NULL,
  cohort_definition_id INTEGER NOT NULL,
  pathway_analysis_id  INTEGER NOT NULL ,
	CONSTRAINT [PK_pathway_target_cohort] PRIMARY KEY (id),
	CONSTRAINT FK_ptc_cd_id
		FOREIGN KEY (cohort_definition_id)
		REFERENCES ${ohdsiSchema}.cohort_definition(id),
	CONSTRAINT FK_ptc_pa_id
		FOREIGN KEY (pathway_analysis_id)
		REFERENCES ${ohdsiSchema}.pathway_analysis(id)
);

CREATE TABLE ${ohdsiSchema}.pathway_event_cohort
(
  id                   INTEGER,
  name                 VARCHAR(255) NOT NULL,
  cohort_definition_id INTEGER NOT NULL,
  pathway_analysis_id  INTEGER NOT NULL,
  is_deleted           INTEGER DEFAULT 0,
	CONSTRAINT [PK_pathway_event_cohort] PRIMARY KEY (id),
	CONSTRAINT FK_pec_cd_id
		FOREIGN KEY (cohort_definition_id)
		REFERENCES ${ohdsiSchema}.cohort_definition(id),
	CONSTRAINT FK_pec_pa_id
		FOREIGN KEY (pathway_analysis_id)
		REFERENCES ${ohdsiSchema}.pathway_analysis(id)
);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analyses:post', 'Create Pathways Analysis'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'pathway-analyses:get', 'Get Pathways Analyses list');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT NEXT VALUE FOR ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE, sr.id, sp.id 
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'pathway-analyses:post',
  'pathway-analyses:get'
)
AND sr.name IN ('Atlas users');

-- Spring Security Migration

-- New user: anonymous with id = -1, name is 'anonymous'

INSERT INTO ${ohdsiSchema}.sec_user (id, login, name, origin)
VALUES (-1, 'anonymous', 'Anonymous', 'SYSTEM');

INSERT INTO ${ohdsiSchema}.sec_role (id, name, system_role)
VALUES (-1, 'anonymous', true);

INSERT INTO ${ohdsiSchema}.sec_user_role (id, user_id, role_id, origin)
VALUES (nextval('${ohdsiSchema}.sec_user_role_sequence'), -1, -1, 'SYSTEM');

-- migrate all null created_by to be associated to the anonymous user

UPDATE ${ohdsiSchema}.cohort_definition set created_by_id = -1 where created_by_id is null;
UPDATE ${ohdsiSchema}.concept_set set created_by_id = -1 where created_by_id is null;
UPDATE ${ohdsiSchema}.fe_analysis set created_by_id = -1 where created_by_id is null and type <> 'PRESET';
UPDATE ${ohdsiSchema}.ir_analysis set created_by_id = -1 where created_by_id is null;
UPDATE ${ohdsiSchema}.pathway_analysis set created_by_id = -1 where created_by_id is null;
UPDATE ${ohdsiSchema}.reusable set created_by_id = -1 where created_by_id is null;

-- Introduce session table

CREATE TABLE ${ohdsiSchema}.sec_session (
    session_id      UUID PRIMARY KEY,
    login           VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    expires_at      TIMESTAMP NOT NULL,
    revoked         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_sec_session_login
    ON ${ohdsiSchema}.sec_session(login);


-- define new sec_ tables for permission assignments

CREATE TABLE ${ohdsiSchema}.sec_concept_set(
    role_id int,
    concept_set_id int,
    access_type varchar(50) NOT NULL,
    CONSTRAINT pk_sec_concept_set PRIMARY KEY (role_id, concept_set_id, access_type),
    CONSTRAINT fk_scs_concept_set_id
        FOREIGN KEY (concept_set_id)
        REFERENCES ${ohdsiSchema}.concept_set(concept_set_id),
    CONSTRAINT fk_scs_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)        
);

CREATE TABLE ${ohdsiSchema}.sec_cohort_definition(
    role_id int,
    cohort_definition_id int,
    access_type varchar(50) NOT NULL,
    CONSTRAINT pk_sec_cohort_definition PRIMARY KEY (role_id, cohort_definition_id, access_type),
    CONSTRAINT fk_scd_cohort_definition_id
        FOREIGN KEY (cohort_definition_id)
        REFERENCES ${ohdsiSchema}.cohort_definition(id),
    CONSTRAINT fk_scd_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)        
);

CREATE TABLE ${ohdsiSchema}.sec_cohort_characterization(
    role_id int,
    cohort_characterization_id int,
    access_type varchar(50) NOT NULL,
    CONSTRAINT pk_sec_cohort_characterization PRIMARY KEY (role_id, cohort_characterization_id, access_type),
    CONSTRAINT fk_scc_cohort_characterization_id
        FOREIGN KEY (cohort_characterization_id)
        REFERENCES ${ohdsiSchema}.cohort_characterization(id),
    CONSTRAINT fk_scc_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)        
);

CREATE TABLE ${ohdsiSchema}.sec_ir_analysis(
    role_id int,
    ir_id int,
    access_type varchar(50) NOT NULL,
    CONSTRAINT pk_sec_ir_analysis PRIMARY KEY (role_id, ir_id, access_type),
    CONSTRAINT fk_sia_ir_analysis_id
        FOREIGN KEY (ir_id)
        REFERENCES ${ohdsiSchema}.ir_analysis(id),
    CONSTRAINT fk_sia_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)        
);

CREATE TABLE ${ohdsiSchema}.sec_fe_analysis(
    role_id int,
    fe_analysis_id int,
    access_type varchar(50) NOT NULL,
    CONSTRAINT pk_sec_fe_analysis PRIMARY KEY (role_id, fe_analysis_id, access_type),
    CONSTRAINT fk_sfa_fe_analysis_id
        FOREIGN KEY (fe_analysis_id)
        REFERENCES ${ohdsiSchema}.fe_analysis(id),
    CONSTRAINT fk_sfa_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)        
);

CREATE TABLE ${ohdsiSchema}.sec_pathway_analysis(
    role_id int,
    pathway_analysis_id int,
    access_type varchar(50) NOT NULL,
    CONSTRAINT pk_sec_pathway_analysis PRIMARY KEY (role_id, pathway_analysis_id, access_type),
    CONSTRAINT fk_spa_pathway_analysis_id
        FOREIGN KEY (pathway_analysis_id)
        REFERENCES ${ohdsiSchema}.pathway_analysis(id),
    CONSTRAINT fk_spa_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)        
);

CREATE TABLE ${ohdsiSchema}.sec_reusable(
    role_id int,
    reusable_id int,
    access_type varchar(50) NOT NULL,
    CONSTRAINT pk_sec_reusable PRIMARY KEY (role_id, reusable_id, access_type),
    CONSTRAINT fk_sr_reusable_id
        FOREIGN KEY (reusable_id)
        REFERENCES ${ohdsiSchema}.reusable(id),
    CONSTRAINT fk_sr_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)        
);

CREATE TABLE ${ohdsiSchema}.sec_source (
    role_id int,
    source_id int,
    access_type varchar(50) NOT NULL,
    CONSTRAINT pk_sec_source PRIMARY KEY (role_id, source_id, access_type),
    CONSTRAINT fk_ss_source_id
        FOREIGN KEY (source_id)
        REFERENCES ${ohdsiSchema}.source(source_id),
    CONSTRAINT fk_ss_sec_role_id
        FOREIGN KEY (role_id)
        REFERENCES ${ohdsiSchema}.sec_role(id)      
);


-- rename old permission tables and drop constraints/sequences

drop sequence ${ohdsiSchema}.sec_permission_id_seq;


ALTER TABLE ${ohdsiSchema}.sec_role_permission
DROP CONSTRAINT fk_role_permission_to_permission;

ALTER TABLE ${ohdsiSchema}.sec_role_permission
DROP CONSTRAINT fk_role_permission_to_role;

ALTER TABLE ${ohdsiSchema}.sec_role_permission
DROP CONSTRAINT pk_sec_role_permission;

ALTER TABLE ${ohdsiSchema}.sec_permission
DROP CONSTRAINT pk_sec_permission;

ALTER TABLE ${ohdsiSchema}.sec_permission
DROP CONSTRAINT permission_unique;




ALTER TABLE ${ohdsiSchema}.sec_permission
RENAME TO sec_permission_legacy;

ALTER TABLE ${ohdsiSchema}.sec_role_permission
RENAME TO sec_role_permission_legacy;

-- NOTE: sec_permission_legacy and sec_role_permission_legacy are intentionally retained
-- for rollback verification. Drop manually after confirming migration success.

-- populate sec_{entity} tables based on permission assignments

--- Start: CONCEPT_SET

WITH read_permission_templates(template) AS (
  VALUES
    ('conceptset:%s:get'),
    ('conceptset:%s:expression:get'),
    ('conceptset:%s:annotation:get'),
    ('conceptset:%s:version:*:expression:get')
),
expanded_permissions AS (
	SELECT
		cs.concept_set_id,
		r.id as created_by_id,
		r.name as role_name,
		u.login,
		replace(replace(t.template, '%s', cs.concept_set_id::text), '*', '\*') AS permission_pattern
	FROM ${ohdsiSchema}.concept_set cs
	JOIN ${ohdsiSchema}.sec_user_role ur on ur.user_id = cs.created_by_id
	JOIN ${ohdsiSchema}.sec_role r on r.id = ur.role_id and r.system_role is false
	join ${ohdsiSchema}.sec_user u on ur.user_id = u.id
	CROSS JOIN read_permission_templates t
	where r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.concept_set_id,
    p.role_id,
    ep.created_by_id
  FROM role_permissions p
  join expanded_permissions ep on p.permission_value = ep.permission_pattern
)
insert into ${ohdsiSchema}.sec_concept_set (role_id, concept_set_id, access_type)
SELECT DISTINCT
  role_id,
  concept_set_id,
  'READ' AS permission
FROM matched_permissions
WHERE role_id <> created_by_id;

WITH write_permission_templates(template) AS (
  VALUES
    ('conceptset:%s:put'),
    ('conceptset:%s:items:put'),
    ('conceptset:%s:annotation:*:delete'),
    ('conceptset:%s:delete')
),
expanded_permissions AS (
	SELECT
		cs.concept_set_id,
		r.id as created_by_id,
		r.name as role_name,
		u.login,
		replace(replace(t.template, '%s', cs.concept_set_id::text), '*', '\*') AS permission_pattern
	FROM ${ohdsiSchema}.concept_set cs
	JOIN ${ohdsiSchema}.sec_user_role ur on ur.user_id = cs.created_by_id
	JOIN ${ohdsiSchema}.sec_role r on r.id = ur.role_id and r.system_role is false
	join ${ohdsiSchema}.sec_user u on ur.user_id = u.id
	CROSS JOIN write_permission_templates t
	where r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.concept_set_id,
    p.role_id,
    ep.created_by_id
  FROM role_permissions p
  join expanded_permissions ep on p.permission_value = ep.permission_pattern
)
insert into ${ohdsiSchema}.sec_concept_set (role_id, concept_set_id, access_type)
SELECT DISTINCT
  role_id,
  concept_set_id,
  'WRITE' AS permission
FROM matched_permissions
WHERE role_id <> created_by_id;

-- End: CONCEPT_SET

-- Start: COHORT_DEFINITION

WITH read_permission_templates(template) AS (
  VALUES
    ('cohortdefinition:%s:get'),
    ('cohortdefinition:%s:info:get'),
    ('cohortdefinition:%s:version:get'),
    ('cohortdefinition:%s:version:*:get')
),
expanded_permissions AS (
	SELECT
		cd.id as cohort_definition_id,
		r.id as created_by_id,
		r.name as role_name,
		u.login,
		replace(replace(t.template, '%s', cd.id::text),'*', '\*') AS permission_pattern
	FROM ${ohdsiSchema}.cohort_definition cd
	JOIN ${ohdsiSchema}.sec_user_role ur on ur.user_id = cd.created_by_id
	JOIN ${ohdsiSchema}.sec_role r on r.id = ur.role_id and r.system_role is false
	join ${ohdsiSchema}.sec_user u on ur.user_id = u.id
	CROSS JOIN read_permission_templates t
	where r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.cohort_definition_id,
    p.role_id,
    ep.created_by_id
  FROM role_permissions p
  join expanded_permissions ep on p.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_cohort_definition (role_id, cohort_definition_id, access_type)
SELECT DISTINCT
  role_id,
  cohort_definition_id,
  'READ' AS access_type
FROM matched_permissions
WHERE role_id <> created_by_id;

WITH write_permission_templates(template) AS (
  VALUES
    ('cohortdefinition:%s:put'),
    ('cohortdefinition:%s:delete'),
    ('cohortdefinition:%s:check:post')
),
expanded_permissions AS (
	SELECT
		cd.id as cohort_definition_id,
		r.id as created_by_id,
		r.name as role_name,
		u.login,
		replace(replace(t.template, '%s', cd.id::text),'*', '\*') AS permission_pattern
	FROM ${ohdsiSchema}.cohort_definition cd
	JOIN ${ohdsiSchema}.sec_user_role ur on ur.user_id = cd.created_by_id
	JOIN ${ohdsiSchema}.sec_role r on r.id = ur.role_id and r.system_role is false
	join ${ohdsiSchema}.sec_user u on ur.user_id = u.id
	CROSS JOIN write_permission_templates t
	where r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.cohort_definition_id,
    p.role_id,
    ep.created_by_id
  FROM role_permissions p
  join expanded_permissions ep on p.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_cohort_definition (role_id, cohort_definition_id, access_type)
SELECT DISTINCT
  role_id,
  cohort_definition_id,
  'WRITE' AS access_type
FROM matched_permissions
WHERE role_id <> created_by_id;

-- END: COHORT_DEFINITION

-- START: COHORT_CHARACTERIZATION:
WITH read_permission_templates(template) AS (
  VALUES
    ('cohort-characterization:%s:get'),
    ('cohort-characterization:%s:generation:get'),
    ('cohort-characterization:%s:design:get'),
    ('cohort-characterization:design:%s:get'),
    ('cohort-characterization:%s:version:get'),
    ('cohort-characterization:%s:version:*:get')
),
expanded_permissions AS (
	SELECT
		cc.id as cohort_characterization_id,
		r.id as created_by_id,
		r.name as role_name,
		u.login,
		replace(replace(t.template, '%s', cc.id::text),'*','\*') AS permission_pattern
	FROM ${ohdsiSchema}.cohort_characterization cc
	JOIN ${ohdsiSchema}.sec_user_role ur on ur.user_id = cc.created_by_id
	JOIN ${ohdsiSchema}.sec_role r on r.id = ur.role_id and r.system_role is false
	join ${ohdsiSchema}.sec_user u on ur.user_id = u.id
	CROSS JOIN read_permission_templates t
	where r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.cohort_characterization_id,
    p.role_id,
    ep.created_by_id
  FROM role_permissions p
  join expanded_permissions ep on p.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_cohort_characterization
  (role_id, cohort_characterization_id, access_type)
SELECT DISTINCT
  role_id,
  cohort_characterization_id,
  'READ' AS access_type
FROM matched_permissions
WHERE role_id <> created_by_id;

WITH write_permission_templates(template) AS (
  VALUES
    ('cohort-characterization:%s:put'),
    ('cohort-characterization:%s:delete')
),
expanded_permissions AS (
	SELECT
		cc.id as cohort_characterization_id,
		r.id as created_by_id,
		r.name as role_name,
		u.login,
		replace(replace(t.template, '%s', cc.id::text),'*','\*') AS permission_pattern
	FROM ${ohdsiSchema}.cohort_characterization cc
	JOIN ${ohdsiSchema}.sec_user_role ur on ur.user_id = cc.created_by_id
	JOIN ${ohdsiSchema}.sec_role r on r.id = ur.role_id and r.system_role is false
	join ${ohdsiSchema}.sec_user u on ur.user_id = u.id
	CROSS JOIN write_permission_templates t
	where r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.cohort_characterization_id,
    p.role_id,
    ep.created_by_id
  FROM role_permissions p
  join expanded_permissions ep on p.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_cohort_characterization
  (role_id, cohort_characterization_id, access_type)
SELECT DISTINCT
  role_id,
  cohort_characterization_id,
  'WRITE' AS access_type
FROM matched_permissions
WHERE role_id <> created_by_id;

-- END: COHORT_CHARACTERIZATION:

-- Start: INCIDENCE_RATE:
WITH read_permission_templates(template) AS (
  VALUES
    ('ir:%s:get'),
    ('ir:%s:version:get'),
    ('ir:%s:version:*:get'),
    ('ir:%s:copy:get'),
    ('ir:%s:info:get'),
    ('ir:%s:design:get')
),
expanded_permissions AS (
  SELECT
    ir.id AS ir_id,
    r.id AS created_by_id,
    r.name AS role_name,
    u.login,
    replace(replace(t.template, '%s', ir.id::text), '*', '\*') AS permission_pattern
  FROM ${ohdsiSchema}.ir_analysis ir
  JOIN ${ohdsiSchema}.sec_user_role ur
    ON ur.user_id = ir.created_by_id
  JOIN ${ohdsiSchema}.sec_role r
    ON r.id = ur.role_id
   AND r.system_role IS FALSE
  JOIN ${ohdsiSchema}.sec_user u
    ON ur.user_id = u.id
  CROSS JOIN read_permission_templates t
  WHERE r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.ir_id,
    rp.role_id,
    ep.created_by_id
  FROM role_permissions rp
  JOIN expanded_permissions ep
    ON rp.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_ir_analysis (role_id, ir_id, access_type)
SELECT DISTINCT
  role_id,
  ir_id,
  'READ' AS access_type
FROM matched_permissions
WHERE role_id <> created_by_id;

WITH write_permission_templates(template) AS (
  VALUES
    ('ir:%s:put'),
    ('ir:%s:delete')
),
expanded_permissions AS (
  SELECT
    ir.id AS ir_id,
    r.id AS created_by_id,
    r.name AS role_name,
    u.login,
    replace(replace(t.template, '%s', ir.id::text), '*', '\*') AS permission_pattern
  FROM ${ohdsiSchema}.ir_analysis ir
  JOIN ${ohdsiSchema}.sec_user_role ur
    ON ur.user_id = ir.created_by_id
  JOIN ${ohdsiSchema}.sec_role r
    ON r.id = ur.role_id
   AND r.system_role IS FALSE
  JOIN ${ohdsiSchema}.sec_user u
    ON ur.user_id = u.id
  CROSS JOIN write_permission_templates t
  WHERE r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.ir_id,
    rp.role_id,
    ep.created_by_id
  FROM role_permissions rp
  JOIN expanded_permissions ep
    ON rp.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_ir_analysis (role_id, ir_id, access_type)
SELECT DISTINCT
  role_id,
  ir_id,
  'WRITE' AS access_type
FROM matched_permissions
WHERE role_id <> created_by_id;

-- End: INCIDENCE_RATE:

-- Start: FEATURE_ANALYSIS:
WITH read_permission_templates(template) AS (
  VALUES
    ('feature-analysis:%s:get')
),
expanded_permissions AS (
  SELECT
    fe.id AS fe_analysis_id,
    r.id AS created_by_id,
    r.name AS role_name,
    u.login,
    replace(replace(t.template, '%s', fe.id::text), '*', '\*') AS permission_pattern
  FROM ${ohdsiSchema}.fe_analysis fe
  JOIN ${ohdsiSchema}.sec_user_role ur
    ON ur.user_id = fe.created_by_id
  JOIN ${ohdsiSchema}.sec_role r
    ON r.id = ur.role_id
   AND r.system_role IS FALSE
  JOIN ${ohdsiSchema}.sec_user u
    ON ur.user_id = u.id
  CROSS JOIN read_permission_templates t
  WHERE r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.fe_analysis_id,
    rp.role_id,
    ep.created_by_id
  FROM role_permissions rp
  JOIN expanded_permissions ep
    ON rp.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_fe_analysis (role_id, fe_analysis_id, access_type)
SELECT DISTINCT
  role_id,
  fe_analysis_id,
  'READ' AS access_type
FROM matched_permissions
WHERE role_id <> created_by_id;

WITH write_permission_templates(template) AS (
  VALUES
    ('feature-analysis:%s:put'),
    ('feature-analysis:%s:delete')
),
expanded_permissions AS (
  SELECT
    fe.id AS fe_analysis_id,
    r.id AS created_by_id,
    r.name AS role_name,
    u.login,
    replace(replace(t.template, '%s', fe.id::text), '*', '\*') AS permission_pattern
  FROM ${ohdsiSchema}.fe_analysis fe
  JOIN ${ohdsiSchema}.sec_user_role ur
    ON ur.user_id = fe.created_by_id
  JOIN ${ohdsiSchema}.sec_role r
    ON r.id = ur.role_id
   AND r.system_role IS FALSE
  JOIN ${ohdsiSchema}.sec_user u
    ON ur.user_id = u.id
  CROSS JOIN write_permission_templates t
  WHERE r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.fe_analysis_id,
    rp.role_id,
    ep.created_by_id
  FROM role_permissions rp
  JOIN expanded_permissions ep
    ON rp.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_fe_analysis (role_id, fe_analysis_id, access_type)
SELECT DISTINCT
  role_id,
  fe_analysis_id,
  'WRITE' AS access_type
FROM matched_permissions
WHERE role_id <> created_by_id;

-- End: FEATURE_ANALYSIS:

-- Start: PATHWAY_ANALYSIS
WITH read_permission_templates(template) AS (
  VALUES
    ('pathway-analysis:%s:get'),
    ('pathway-analysis:%s:generation:get'),
    ('pathway-analysis:%s:version:get'),
    ('pathway-analysis:%s:version:*:get')
),
expanded_permissions AS (
  SELECT
    pa.id AS pathway_analysis_id,
    r.id AS created_by_id,
    replace(replace(t.template, '%s', pa.id::text), '*', '\*') AS permission_pattern
  FROM ${ohdsiSchema}.pathway_analysis pa
  JOIN ${ohdsiSchema}.sec_user_role ur
    ON ur.user_id = pa.created_by_id
  JOIN ${ohdsiSchema}.sec_role r
    ON r.id = ur.role_id
   AND r.system_role IS FALSE
  JOIN ${ohdsiSchema}.sec_user u
    ON u.id = ur.user_id
  CROSS JOIN read_permission_templates t
  WHERE r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.pathway_analysis_id,
    rp.role_id,
    ep.created_by_id
  FROM role_permissions rp
  JOIN expanded_permissions ep
    ON rp.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_pathway_analysis (role_id, pathway_analysis_id, access_type)
SELECT DISTINCT
  role_id,
  pathway_analysis_id,
  'READ'
FROM matched_permissions
WHERE role_id <> created_by_id;

WITH write_permission_templates(template) AS (
  VALUES
    ('pathway-analysis:%s:put'),
    ('pathway-analysis:%s:sql:*:get'),
    ('pathway-analysis:%s:delete')
),
expanded_permissions AS (
  SELECT
    pa.id AS pathway_analysis_id,
    r.id AS created_by_id,
    replace(replace(t.template, '%s', pa.id::text), '*', '\*') AS permission_pattern
  FROM ${ohdsiSchema}.pathway_analysis pa
  JOIN ${ohdsiSchema}.sec_user_role ur
    ON ur.user_id = pa.created_by_id
  JOIN ${ohdsiSchema}.sec_role r
    ON r.id = ur.role_id
   AND r.system_role IS FALSE
  JOIN ${ohdsiSchema}.sec_user u
    ON u.id = ur.user_id
  CROSS JOIN write_permission_templates t
  WHERE r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.pathway_analysis_id,
    rp.role_id,
    ep.created_by_id
  FROM role_permissions rp
  JOIN expanded_permissions ep
    ON rp.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_pathway_analysis (role_id, pathway_analysis_id, access_type)
SELECT DISTINCT
  role_id,
  pathway_analysis_id,
  'WRITE'
FROM matched_permissions
WHERE role_id <> created_by_id;

-- End: PATHWAY_ANALYSIS

-- Start: REUSABLE
WITH read_permission_templates(template) AS (
  VALUES
    ('reusable:%s:get'),
    ('reusable:%s:expression:get'),
    ('reusable:%s:version:*:get')
),
expanded_permissions AS (
  SELECT
    rbl.id AS reusable_id,
    r.id AS created_by_id,
    replace(replace(t.template, '%s', rbl.id::text), '*', '\*') AS permission_pattern
  FROM ${ohdsiSchema}.reusable rbl
  JOIN ${ohdsiSchema}.sec_user_role ur
    ON ur.user_id = rbl.created_by_id
  JOIN ${ohdsiSchema}.sec_role r
    ON r.id = ur.role_id
   AND r.system_role IS FALSE
  JOIN ${ohdsiSchema}.sec_user u
    ON u.id = ur.user_id
  CROSS JOIN read_permission_templates t
  WHERE r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.reusable_id,
    rp.role_id,
    ep.created_by_id
  FROM role_permissions rp
  JOIN expanded_permissions ep
    ON rp.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_reusable (role_id, reusable_id, access_type)
SELECT DISTINCT
  role_id,
  reusable_id,
  'READ'
FROM matched_permissions
WHERE role_id <> created_by_id;

WITH write_permission_templates(template) AS (
  VALUES
    ('reusable:%s:delete'),
    ('reusable:%s:put')
),
expanded_permissions AS (
  SELECT
    rbl.id AS reusable_id,
    r.id AS created_by_id,
    replace(replace(t.template, '%s', rbl.id::text), '*', '\*') AS permission_pattern
  FROM ${ohdsiSchema}.reusable rbl
  JOIN ${ohdsiSchema}.sec_user_role ur
    ON ur.user_id = rbl.created_by_id
  JOIN ${ohdsiSchema}.sec_role r
    ON r.id = ur.role_id
   AND r.system_role IS FALSE
  JOIN ${ohdsiSchema}.sec_user u
    ON u.id = ur.user_id
  CROSS JOIN write_permission_templates t
  WHERE r.name = u.login
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.reusable_id,
    rp.role_id,
    ep.created_by_id
  FROM role_permissions rp
  JOIN expanded_permissions ep
    ON rp.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_reusable (role_id, reusable_id, access_type)
SELECT DISTINCT
  role_id,
  reusable_id,
  'WRITE'
FROM matched_permissions
WHERE role_id <> created_by_id;

-- End: REUSABLE

-- Start: SOURCE

-- WRITE Access Type (WebAPI 2.x read templates -> WRITE access)
WITH write_permission_templates(template) AS (
  VALUES
    ('cohortdefinition:*:report:%s:get'),
    ('cohortdefinition:*:generate:%s:get'),
    ('cohortdefinition:*:cancel:%s:get'),
    ('vocabulary:%s:*:get'),
    ('vocabulary:%s:included-concepts:count:post'),
    ('vocabulary:%s:resolveConceptSetExpression:post'),
    ('vocabulary:%s:lookup:identifiers:post'),
    ('vocabulary:%s:lookup:identifiers:ancestors:post'),
    ('vocabulary:%s:lookup:mapped:post'),
    ('vocabulary:%s:lookup:recommended:post'),
    ('vocabulary:%s:compare:post'),
    ('vocabulary:%s:optimize:post'),
    ('vocabulary:%s:concept:*:get'),
    ('vocabulary:%s:concept:*:related:get'),
    ('vocabulary:%s:search:post'),
    ('vocabulary:%s:search:*:get'),
    ('cdmresults:%s:*:get'),
    ('cdmresults:%s:conceptRecordCount:post'),
    ('cdmresults:%s:*:*:get'),
    ('cdmresults:%s:clearcache:post'),
    ('cohortresults:%s:*:*:get'),
    ('cohortresults:%s:*:*:*:get'),
    ('cohortresults:%s:*:healthcareutilization:*:*:get'),
    ('cohortresults:%s:*:healthcareutilization:*:*:*:get'),
    ('ir:*:execute:%s:get'),
    ('ir:*:execute:%s:delete'),
    ('ir:*:info:%s:get'),
    ('ir:*:report:%s:get'),
    ('ir:%s:info:*:delete'),
    ('%s:person:*:get'),
    ('vocabulary:%s:lookup:sourcecodes:post'),
    ('cohort-characterization:*:generation:%s:post'),
    ('cohort-characterization:*:generation:%s:delete'),
    ('pathway-analysis:*:generation:%s:post'),
    ('pathway-analysis:*:generation:%s:delete'),
    ('vocabulary:%s:concept:*:ancestorAndDescendant:get'),
    ('source:%s:access')
),
expanded_permissions AS (
  SELECT
    s.source_id,
    REPLACE(REPLACE(t.template, '%s', s.source_key), '*', '\*') AS permission_pattern
  FROM ${ohdsiSchema}.source s
  CROSS JOIN write_permission_templates t
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  SELECT DISTINCT
    ep.source_id,
    rp.role_id
  FROM role_permissions rp
  JOIN expanded_permissions ep ON rp.permission_value = ep.permission_pattern
)
INSERT INTO ${ohdsiSchema}.sec_source(role_id, source_id, access_type)
SELECT DISTINCT
  m.role_id,
  m.source_id,
  'WRITE' AS access_type
FROM matched_permissions m
;

-- End: SOURCE

-- Global Entitlements (permissions that greant global read/write to entities) and administrative tasks

-- re-create permission and role-permission tables and restart sequences
ALTER SEQUENCE ${ohdsiSchema}.sec_permission_sequence RESTART WITH 1;

CREATE TABLE ${ohdsiSchema}.sec_permission
(
    id integer NOT NULL DEFAULT nextval('${ohdsiSchema}.sec_permission_sequence'),
    value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_sec_permission PRIMARY KEY (id),
    CONSTRAINT uq_sec_permission_value UNIQUE (value)
);

ALTER SEQUENCE ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE RESTART WITH 1;
CREATE TABLE ${ohdsiSchema}.SEC_ROLE_PERMISSION (
  ID INTEGER NOT NULL DEFAULT NEXTVAL('${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE'),
	ROLE_ID INTEGER NOT NULL,
	PERMISSION_ID INTEGER NOT NULL,
  CONSTRAINT pk_sec_role_permission PRIMARY KEY (id),
  CONSTRAINT uq_sec_role_permission UNIQUE (role_id, permission_id),
  CONSTRAINT fk_srp_permission FOREIGN KEY (permission_id)
      REFERENCES ${ohdsiSchema}.sec_permission(id),
  CONSTRAINT fk_srp_role FOREIGN KEY (role_id)
      REFERENCES ${ohdsiSchema}.sec_role(id)
);

insert into ${ohdsiSchema}.sec_permission(id, value, description)
select nextval('${ohdsiSchema}.sec_permission_sequence'), value, description
FROM (
	VALUES
	('*', 'All Permissions'),
	('admin', 'All Admin Permissions'),
	('admin:source', 'Manage Sources'),
	('admin:tags', 'Manage Tags'),
	('admin:tools', 'Manage Tools'),
	('admin:security', 'Manage users, roles, permissions'),
	('admin:cache', 'View and manage chache functions'),
	('admin:run-as', 'Run as another user'),
	('create', 'Create any asset'),
	('create:conceptset', 'Create concept sets'),
	('create:cohort-definition', 'Create cohort definitions'),
	('create:cohort-characterization', 'Create characterization designs'),
	('create:feature-analysis', 'Create feature analysis'),
	('create:incidence', 'Create incidence designs'),
	('create:pathway', 'Create pathway designs'),
	('create:reusable', 'Create reusable components'),
	('read', 'Read any asset'),
	('read:conceptset', 'Read concept sets'),
	('read:cohort-definition', 'Read cohort definitions'),
	('read:cohort-characterization', 'Read characterization designs'),
	('read:feature-analysis', 'Read feature analysis'),
	('read:incidence', 'Read incidence designs'),
	('read:pathway', 'Read pathway designs'),
	('read:reusable', 'Read reusable components'),
	('read:source', 'Read source results'),
	('write', 'Update any asset'),
	('write:conceptset', 'Update concept sets'),
	('write:cohort-definition', 'Update cohort definitions'),
	('write:cohort-characterization', 'Update characterization designs'),
	('write:feature-analysis', 'Update feature analysis'),
	('write:incidence', 'Update incidence designs'),
	('write:pathway', 'Update pathway designs'),
	('write:reusable', 'Update reusable components'),
	('write:source', 'Generate source results')
) p (value, description)
;

WITH perm_map AS (
  SELECT DISTINCT
    p.value AS from_perm,
    CASE
      /* ---------- ADMIN-Privs ---------- */
      WHEN p.value ~ '^source:\*:(put|post|delete)$'
        THEN 'admin:source'
 	  WHEN p.value ~ '^(role:post|role:\*:(put|delete)|role:\*:users:\*:(put|delete)|role:\*:permissions:\*:(put|delete)|user:import:\*:(post|put|delete))$'
  		THEN 'admin:security'		
	  WHEN p.value ~ '^(tag:\*:(put|delete)|tag:(post|management)|tag:multi(Assign|Unassign):post)$'
  		THEN 'admin:tags'
	  WHEN p.value ~ '^(tool:\*:(put|delete)|tool:(post|put))$'
	    THEN 'admin:tools'
	  WHEN p.value ~ '^(cache:.*|cdmresults:clearcache:post)$'
	    THEN 'admin:cache'
	  WHEN p.value ~ '^(user:runas:post)$'
	    THEN 'admin:run-as'	      	
	
      /* ---------- CREATE ---------- */
      WHEN p.value ~ '^conceptset:post$'
        THEN 'create:conceptset'
      WHEN p.value ~ '^cohortdefinition:post$'
        THEN 'create:cohort-definition'
      WHEN p.value ~ '^cohort-characterization:post$'
        THEN 'create:cohort-characterization'
      WHEN p.value ~ '^feature-analysis:post$'
        THEN 'create:feature-analysis'
      WHEN p.value ~ '^ir:post$'
        THEN 'create:incidence'
      WHEN p.value ~ '^pathway-analysis:post$'
        THEN 'create:pathway'
      WHEN p.value ~ '^reusable:post$'
        THEN 'create:reusable'

      /* ---------- READ ---------- */
      WHEN p.value ~ '^conceptset:\*:get$'
        THEN 'read:conceptset'
      WHEN p.value ~ '^cohortdefinition:\*:get$'
        THEN 'read:cohort-definition'
      WHEN p.value ~ '^cohort-characterization:\*:get$'
        THEN 'read:cohort-characterization'
      WHEN p.value ~ '^feature-analysis:\*:get$'
        THEN 'read:feature-analysis'
      WHEN p.value ~ '^ir:\*:get$'
        THEN 'read:incidence'
      WHEN p.value ~ '^pathway-analysis:\*:get$'
        THEN 'read:pathway'
      WHEN p.value ~ '^reusable:\*:get$'
        THEN 'read:reusable'

      /* ---------- WRITE (PUT / DELETE) ---------- */
      WHEN p.value ~ '^conceptset:\*:(put|delete)$'
        THEN 'write:conceptset'
      WHEN p.value ~ '^cohortdefinition:\*:(put|delete)$'
        THEN 'write:cohort-definition'
      WHEN p.value ~ '^cohort-characterization:\*:(put|delete)$'
        THEN 'write:cohort-characterization'
      WHEN p.value ~ '^feature-analysis:\*:(put|delete)$'
        THEN 'write:feature-analysis'
      WHEN p.value ~ '^ir:\*:(put|delete)$'
        THEN 'write:incidence'
      WHEN p.value ~ '^pathway-analysis:\*:(put|delete)$'
        THEN 'write:pathway'
      WHEN p.value ~ '^reusable:\*:(put|delete)$'
        THEN 'write:reusable'

      ELSE NULL
    END AS to_perm
  FROM ${ohdsiSchema}.sec_permission_legacy p
),
role_permissions as (
	select distinct role_id, p.value as permission_value
	from ${ohdsiSchema}.sec_role_permission_legacy rp
	join ${ohdsiSchema}.sec_permission_legacy p on rp.permission_id = p.id
),
matched_permissions AS (
  select 
  	rp.role_id,
	p.id as permission_id,
	p.value,
	pm.from_perm,
	pm.to_perm
  FROM role_permissions rp
  JOIN perm_map pm ON rp.permission_value = pm.from_perm
  join ${ohdsiSchema}.sec_permission p on p.value = pm.to_perm
  join ${ohdsiSchema}.sec_role r on r.id = rp.role_id
)
INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT 
  NEXTVAL('${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE') as id,
  role_id, 
  permission_id
from (
  select distinct
    mp.role_id,
    mp.permission_id
  FROM matched_permissions mp
) m;


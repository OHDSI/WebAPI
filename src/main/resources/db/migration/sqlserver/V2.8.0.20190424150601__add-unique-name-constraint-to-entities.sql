-- Alter Concept Set table

UPDATE ${ohdsiSchema}.concept_set
SET concept_set_name = u.concept_set_name + ' (' + CAST(u.rn AS varchar(15)) + ')'
FROM 
  ${ohdsiSchema}.concept_set, 
  (
    SELECT d.concept_set_id, d.concept_set_name, d.rn
    FROM
    (
      select a.concept_set_name, cd.concept_set_id, ROW_NUMBER() OVER(PARTITION BY a.concept_set_name ORDER BY cd.concept_set_id ASC) rn
      FROM
      (
        select concept_set_name, COUNT(*) cnt
        FROM ${ohdsiSchema}.concept_set
        group by concept_set_name having COUNT(*) > 1
      ) a
      INNER JOIN ${ohdsiSchema}.concept_set cd ON a.concept_set_name = cd.concept_set_name
    ) d 
  ) u
 WHERE u.concept_set_id = ${ohdsiSchema}.concept_set.concept_set_id 
;

ALTER TABLE ${ohdsiSchema}.concept_set ADD CONSTRAINT uq_cs_name UNIQUE (concept_set_name);

-- Alter Cohort Definition table

UPDATE ${ohdsiSchema}.cohort_definition
SET name = u.name + ' (' + CAST(u.rn AS varchar(15)) + ')'
FROM 
  ${ohdsiSchema}.cohort_definition, 
  (
    SELECT d.id, d.name, d.rn
    FROM
    (
      select a.name, cd.id, ROW_NUMBER() OVER(PARTITION BY a.name ORDER BY cd.id ASC) rn
      FROM
      (
        select name, COUNT(*) cnt
        FROM ${ohdsiSchema}.cohort_definition
        group by name having COUNT(*) > 1
      ) a
      INNER JOIN ${ohdsiSchema}.cohort_definition cd ON a.name = cd.name
    ) d 
  ) u
 WHERE u.id = ${ohdsiSchema}.cohort_definition.id 
;

ALTER TABLE ${ohdsiSchema}.cohort_definition ADD CONSTRAINT uq_cd_name UNIQUE (name);

-- Alter Cohort Characterization table
UPDATE ${ohdsiSchema}.cohort_characterization
SET name = u.name + ' (' + CAST(u.rn AS varchar(15)) + ')'
FROM 
  ${ohdsiSchema}.cohort_characterization, 
  (
    SELECT d.id, d.name, d.rn
    FROM
    (
      select a.name, cd.id, ROW_NUMBER() OVER(PARTITION BY a.name ORDER BY cd.id ASC) rn
      FROM
      (
        select name, COUNT(*) cnt
        FROM ${ohdsiSchema}.cohort_characterization
        group by name having COUNT(*) > 1
      ) a
      INNER JOIN ${ohdsiSchema}.cohort_characterization cd ON a.name = cd.name
    ) d 
  ) u
 WHERE u.id = ${ohdsiSchema}.cohort_characterization.id 
;

ALTER TABLE ${ohdsiSchema}.cohort_characterization ADD CONSTRAINT uq_cc_name UNIQUE (name);

-- Alter Fe Analysis Table

UPDATE ${ohdsiSchema}.fe_analysis
SET name = u.name + ' (' + CAST(u.rn AS varchar(15)) + ')'
FROM 
  ${ohdsiSchema}.fe_analysis, 
  (
    SELECT d.id, d.name, d.rn
    FROM
    (
      select a.name, cd.id, ROW_NUMBER() OVER(PARTITION BY a.name ORDER BY cd.id ASC) rn
      FROM
      (
        select name, COUNT(*) cnt
        FROM ${ohdsiSchema}.fe_analysis
        group by name having COUNT(*) > 1
      ) a
      INNER JOIN ${ohdsiSchema}.fe_analysis cd ON a.name = cd.name
    ) d 
  ) u
 WHERE u.id = ${ohdsiSchema}.fe_analysis.id 
;

ALTER TABLE ${ohdsiSchema}.fe_analysis ADD CONSTRAINT uq_fe_name UNIQUE (name);

-- Alter Pathway Analysis Table

UPDATE ${ohdsiSchema}.pathway_analysis
SET name = u.name + ' (' + CAST(u.rn AS varchar(15)) + ')'
FROM 
  ${ohdsiSchema}.pathway_analysis, 
  (
    SELECT d.id, d.name, d.rn
    FROM
    (
      select a.name, cd.id, ROW_NUMBER() OVER(PARTITION BY a.name ORDER BY cd.id ASC) rn
      FROM
      (
        select name, COUNT(*) cnt
        FROM ${ohdsiSchema}.pathway_analysis
        group by name having COUNT(*) > 1
      ) a
      INNER JOIN ${ohdsiSchema}.pathway_analysis cd ON a.name = cd.name
    ) d 
  ) u
 WHERE u.id = ${ohdsiSchema}.pathway_analysis.id 
;

ALTER TABLE ${ohdsiSchema}.pathway_analysis ADD CONSTRAINT uq_pw_name UNIQUE (name);

-- Alter IR Analysis Table

UPDATE ${ohdsiSchema}.ir_analysis
SET name = u.name + ' (' + CAST(u.rn AS varchar(15)) + ')'
FROM 
  ${ohdsiSchema}.ir_analysis, 
  (
    SELECT d.id, d.name, d.rn
    FROM
    (
      select a.name, cd.id, ROW_NUMBER() OVER(PARTITION BY a.name ORDER BY cd.id ASC) rn
      FROM
      (
        select name, COUNT(*) cnt
        FROM ${ohdsiSchema}.ir_analysis
        group by name having COUNT(*) > 1
      ) a
      INNER JOIN ${ohdsiSchema}.ir_analysis cd ON a.name = cd.name
    ) d 
  ) u
 WHERE u.id = ${ohdsiSchema}.ir_analysis.id 
;

ALTER TABLE ${ohdsiSchema}.ir_analysis ADD CONSTRAINT uq_ir_name UNIQUE (name);

-- Alter Estimation table

UPDATE ${ohdsiSchema}.estimation
SET name = u.name + ' (' + CAST(u.rn AS varchar(15)) + ')'
FROM 
  ${ohdsiSchema}.estimation, 
  (
    SELECT d.estimation_id, d.name, d.rn
    FROM
    (
      select a.name, cd.estimation_id, ROW_NUMBER() OVER(PARTITION BY a.name ORDER BY cd.estimation_id ASC) rn
      FROM
      (
        select name, COUNT(*) cnt
        FROM ${ohdsiSchema}.estimation
        group by name having COUNT(*) > 1
      ) a
      INNER JOIN ${ohdsiSchema}.estimation cd ON a.name = cd.name
    ) d 
  ) u
 WHERE u.estimation_id = ${ohdsiSchema}.estimation.estimation_id 
;

ALTER TABLE ${ohdsiSchema}.estimation ADD CONSTRAINT uq_es_name UNIQUE (name);

-- Alter Prediction table

UPDATE ${ohdsiSchema}.prediction
SET name = u.name + ' (' + CAST(u.rn AS varchar(15)) + ')'
FROM 
  ${ohdsiSchema}.prediction, 
  (
    SELECT d.prediction_id, d.name, d.rn
    FROM
    (
      select a.name, cd.prediction_id, ROW_NUMBER() OVER(PARTITION BY a.name ORDER BY cd.prediction_id ASC) rn
      FROM
      (
        select name, COUNT(*) cnt
        FROM ${ohdsiSchema}.prediction
        group by name having COUNT(*) > 1
      ) a
      INNER JOIN ${ohdsiSchema}.prediction cd ON a.name = cd.name
    ) d 
  ) u
 WHERE u.prediction_id = ${ohdsiSchema}.prediction.prediction_id 
;

ALTER TABLE ${ohdsiSchema}.prediction ADD CONSTRAINT uq_pd_name UNIQUE (name);
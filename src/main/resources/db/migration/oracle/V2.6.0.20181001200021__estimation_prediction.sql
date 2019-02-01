-- Estimation
CREATE SEQUENCE ${ohdsiSchema}.estimation_seq;
CREATE TABLE ${ohdsiSchema}.estimation
(
  estimation_id NUMBER(19),
  name VARCHAR(255) NOT NULL,
  type VARCHAR(255) NOT NULL,
  description VARCHAR(1000),
  specification CLOB NOT NULL,
  created_by_id INTEGER,
  created_date  TIMESTAMP,
  modified_by_id INTEGER,
  modified_date  TIMESTAMP,
  CONSTRAINT pk_estimation PRIMARY KEY (estimation_id)
);

ALTER TABLE ${ohdsiSchema}.estimation 
  ADD CONSTRAINT fk_estimation_ser_user_creator FOREIGN KEY (created_by_id)
REFERENCES ${ohdsiSchema}.sec_user (id);

ALTER TABLE ${ohdsiSchema}.estimation 
  ADD CONSTRAINT fk_estimation_ser_user_updater FOREIGN KEY (modified_by_id)
REFERENCES ${ohdsiSchema}.sec_user (id);

-- Prediction
CREATE SEQUENCE ${ohdsiSchema}.prediction_seq;
CREATE TABLE ${ohdsiSchema}.prediction
(
  prediction_id NUMBER(19),
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1000),
  specification CLOB NOT NULL,
  created_by_id INTEGER,
  created_date  TIMESTAMP,
  modified_by_id INTEGER,
  modified_date  TIMESTAMP,
  CONSTRAINT pk_prediction PRIMARY KEY (prediction_id)
);

ALTER TABLE ${ohdsiSchema}.prediction 
  ADD CONSTRAINT fk_prediction_ser_user_creator FOREIGN KEY (created_by_id)
REFERENCES ${ohdsiSchema}.sec_user (id);

ALTER TABLE ${ohdsiSchema}.prediction 
  ADD CONSTRAINT fk_prediction_ser_user_updater FOREIGN KEY (modified_by_id)
REFERENCES ${ohdsiSchema}.sec_user (id);
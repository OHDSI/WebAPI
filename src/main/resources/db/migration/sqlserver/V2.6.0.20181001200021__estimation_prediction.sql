-- Estimation
CREATE SEQUENCE ${ohdsiSchema}.estimation_seq START WITH 1;
CREATE TABLE ${ohdsiSchema}.estimation
(
  estimation_id INTEGER NOT NULL CONSTRAINT df_estimation_id DEFAULT NEXT VALUE FOR ${ohdsiSchema}.estimation_seq,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(255) NOT NULL,
  description VARCHAR(1000),
  specification VARCHAR(MAX) NOT NULL,
  created_by_id      INTEGER,
  created_date       DATETIME,
  modified_by_id     INTEGER,
  modified_date      DATETIME,
  CONSTRAINT pk_estimation PRIMARY KEY (estimation_id)
);

ALTER TABLE ${ohdsiSchema}.estimation 
  ADD CONSTRAINT fk_estimation_ser_user_creator FOREIGN KEY (created_by_id)
REFERENCES ${ohdsiSchema}.sec_user(id)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE ${ohdsiSchema}.estimation 
  ADD CONSTRAINT fk_estimation_ser_user_updater FOREIGN KEY (modified_by_id)
REFERENCES ${ohdsiSchema}.sec_user(id)
ON UPDATE NO ACTION ON DELETE NO ACTION;

-- Prediction
CREATE SEQUENCE ${ohdsiSchema}.prediction_seq START WITH 1;
CREATE TABLE ${ohdsiSchema}.prediction
(
  prediction_id INTEGER NOT NULL CONSTRAINT df_prediction_id DEFAULT NEXT VALUE FOR ${ohdsiSchema}.prediction_seq,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1000),
  specification VARCHAR(MAX) NOT NULL,
  created_by_id INTEGER,
  created_date DATETIME,
  modified_by_id INTEGER,
  modified_date  DATETIME,
  CONSTRAINT pk_prediction PRIMARY KEY (prediction_id)
);

ALTER TABLE ${ohdsiSchema}.prediction 
  ADD CONSTRAINT fk_prediction_ser_user_creator FOREIGN KEY (created_by_id)
REFERENCES ${ohdsiSchema}.sec_user(id)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE ${ohdsiSchema}.prediction 
  ADD CONSTRAINT fk_prediction_ser_user_updater FOREIGN KEY (modified_by_id)
REFERENCES ${ohdsiSchema}.sec_user(id)
ON UPDATE NO ACTION ON DELETE NO ACTION;
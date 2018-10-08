-- Estimation
CREATE SEQUENCE ${ohdsiSchema}.estimation_seq START WITH 1;
CREATE TABLE ${ohdsiSchema}.estimation
(
  estimation_id INTEGER NOT NULL DEFAULT NEXTVAL('estimation_seq'),
  name character varying(255) NOT NULL,
  type character varying(255) NOT NULL,
  description character varying(1000),
  specification text NOT NULL,
  created_by_id      INTEGER,
  created_date       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
  modified_by_id     INTEGER,
  modified_date      TIMESTAMP WITH TIME ZONE,
  CONSTRAINT pk_estimation PRIMARY KEY (estimation_id)
);

ALTER TABLE ${ohdsiSchema}.estimation 
  ADD CONSTRAINT fk_estimation_ser_user_creator FOREIGN KEY (created_by_id)
REFERENCES ${ohdsiSchema}.sec_user (id)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE ${ohdsiSchema}.estimation 
  ADD CONSTRAINT fk_estimation_ser_user_updater FOREIGN KEY (modified_by_id)
REFERENCES ${ohdsiSchema}.sec_user (id)
ON UPDATE NO ACTION ON DELETE NO ACTION;

-- Prediction
CREATE SEQUENCE ${ohdsiSchema}.prediction_seq START WITH 1;
CREATE TABLE ${ohdsiSchema}.prediction
(
  prediction_id INTEGER NOT NULL DEFAULT NEXTVAL('prediction_seq'),
  name character varying(255) NOT NULL,
  description character varying(1000),
  specification text NOT NULL,
  created_by_id      INTEGER,
  created_date       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
  modified_by_id     INTEGER,
  modified_date      TIMESTAMP WITH TIME ZONE,
  CONSTRAINT pk_prediction PRIMARY KEY (prediction_id)
);

ALTER TABLE ${ohdsiSchema}.prediction 
  ADD CONSTRAINT fk_prediction_ser_user_creator FOREIGN KEY (created_by_id)
REFERENCES ${ohdsiSchema}.sec_user (id)
ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE ${ohdsiSchema}.prediction 
  ADD CONSTRAINT fk_prediction_ser_user_updater FOREIGN KEY (modified_by_id)
REFERENCES ${ohdsiSchema}.sec_user (id)
ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE TABLE ${ohdsiSchema}.tool(
  id INTEGER NOT NULL,
  name VARCHAR(255) NOT NULL,
  url VARCHAR(1000) NOT NULL,
  description VARCHAR(1000),
  created_by_id INTEGER NOT NULL,
  modified_by_id INTEGER NOT NULL,
  is_enabled bool, 
  created_date DATE,
  modified_date DATE
);

ALTER TABLE ${ohdsiSchema}.tool ADD (
  CONSTRAINT PK_tool PRIMARY KEY (id));

ALTER TABLE ${ohdsiSchema}.tool
  ADD CONSTRAINT fk_tool_ser_user_creator FOREIGN KEY (created_by_id)
  REFERENCES ${ohdsiSchema}.SEC_USER (id)
  ON DELETE CASCADE;
  

ALTER TABLE ${ohdsiSchema}.tool
  ADD CONSTRAINT fk_tool_ser_user_updater FOREIGN KEY (modified_by_id)
  REFERENCES ${ohdsiSchema}.SEC_USER (id)
  ON DELETE CASCADE;
  

CREATE SEQUENCE ${ohdsiSchema}.tool_seq START WITH 1;

CREATE OR REPLACE TRIGGER ${ohdsiSchema}.tool_bir
  BEFORE INSERT ON ${ohdsiSchema}.tool
  FOR EACH ROW
  BEGIN
    SELECT ${ohdsiSchema}.tool_seq.nextval INTO :new.id FROM dual;
  END;

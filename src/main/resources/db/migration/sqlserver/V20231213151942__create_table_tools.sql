CREATE TABLE ${ohdsiSchema}.tool (
	id INTEGER PRIMARY KEY,
	name VARCHAR (255) NOT NULL,
	url VARCHAR (1000) NOT NULL,
	description VARCHAR (1000),
	is_enabled BIT,
	created_by_id INTEGER,
	modified_by_id INTEGER,
	created_date DATE,
	modified_date DATE
);


ALTER TABLE ${ohdsiSchema}.tool ADD CONSTRAINT fk_tool_ser_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

ALTER TABLE ${ohdsiSchema}.tool ADD CONSTRAINT fk_tool_ser_user_updater FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

CREATE SEQUENCE ${ohdsiSchema}.tool_seq START WITH 1 INCREMENT BY 1 MAXVALUE 9223372036854775807 NO CYCLE;
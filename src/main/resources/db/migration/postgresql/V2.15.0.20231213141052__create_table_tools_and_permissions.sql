CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.tool (
  id BIGINT,
  name VARCHAR(255) NOT NULL,
  url VARCHAR(1000) NOT NULL,
  description VARCHAR(1000),
  is_enabled BOOLEAN,
  created_by_id INTEGER,
  modified_by_id INTEGER,
  created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
  modified_date TIMESTAMP WITH TIME ZONE
);

ALTER TABLE ${ohdsiSchema}.tool ADD CONSTRAINT PK_tool PRIMARY KEY (id);

ALTER TABLE ${ohdsiSchema}.tool ADD CONSTRAINT fk_tool_ser_user_creator FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.tool ADD CONSTRAINT fk_tool_ser_user_updater FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

CREATE SEQUENCE ${ohdsiSchema}.tool_seq START WITH 1 INCREMENT BY 1 MAXVALUE 9223372036854775807 NO CYCLE;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tool:post', 'Create Tool');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tool:put', 'Update Tool');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tool:get', 'List Tools');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tool:*:get', 'View Tool');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'tool:*:delete', 'Delete Tool');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'tool:post',
    'tool:put',
    'tool:get',
    'tool:*:get',
    'tool:*:delete'
    ) AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'tool:get',
    'tool:*:get'
    ) AND sr.name IN ('Atlas users');

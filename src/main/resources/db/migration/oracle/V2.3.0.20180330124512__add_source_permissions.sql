INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(300, 'source:post', 'Create source');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id) VALUES(1, 300);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(301, 'source:*:put', 'Edit source');
INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id) VALUES (1, 301);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(302, 'source:*:delete', 'Delete source');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id) VALUES(1, 302);
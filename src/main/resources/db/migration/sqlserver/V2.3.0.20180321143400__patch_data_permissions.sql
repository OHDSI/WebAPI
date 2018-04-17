insert into ${ohdsiSchema}.sec_permission(id, value, description) values(27, 'conceptset:*:delete', 'Delete Concept Set');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(3, 27);

insert into ${ohdsiSchema}.sec_permission values (31, 'conceptset:*:put', 'Update Concept Set');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(3, 31);

insert into ${ohdsiSchema}.sec_permission values (32, 'conceptset:*:items:put', 'Update Items of Concept Set');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(3, 32);

insert into ${ohdsiSchema}.sec_permission values (66, 'cohortdefinition:delete', 'Delete Cohort Definition');
insert into ${ohdsiSchema}.sec_permission values (67, 'cohortdefinition:*:delete', 'Delete Cohort Definition');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(5, 66);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(5, 67);

insert into ${ohdsiSchema}.sec_permission values (70, 'plp:*:put', 'Update Patient Level Prediction');
insert into ${ohdsiSchema}.sec_permission values (71, 'plp:*:delete', 'Delete Patient Level Prediction');
insert into ${ohdsiSchema}.sec_permission values (72, 'plp:*:copy:get', 'Copy Patient Level Prediction');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 70);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 71);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 72);

insert into ${ohdsiSchema}.sec_permission values (80, 'ir:*:copy:get', 'Copy incidence rate');
insert into ${ohdsiSchema}.sec_permission values (81, 'ir:*:delete', 'Delete incidence rate');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 80);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 81);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(90, 'comparativecohortanalysis:*:copy:get', 'Copy estimations');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 90);
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(91, 'comparativecohortanalysis:*:delete', 'Delete estimation');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 91);
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(92, 'comparativecohortanalysis:*:put', 'Update estimation');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 92);

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
    SELECT 10, p.id
    FROM ${ohdsiSchema}.sec_permission p
		LEFT JOIN ${ohdsiSchema}.SEC_ROLE_PERMISSION rp ON rp.permission_id = p.id AND rp.role_id = 10
    WHERE p.id < 200 AND rp.permission_id is null;

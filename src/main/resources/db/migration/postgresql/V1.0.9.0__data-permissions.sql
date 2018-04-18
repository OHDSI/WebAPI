insert into ${ohdsiSchema}.sec_permission(id, value, description) values(27, 'conceptset:*:delete', 'Delete Concept Set');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(3, 27);

insert into ${ohdsiSchema}.sec_permission values (31, 'conceptset:*:put', 'Update Concept Set');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(3, 31);

insert into ${ohdsiSchema}.sec_permission values (32, 'conceptset:*:items:put', 'Update Items of Concept Set');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(3, 32);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(34, 'conceptset:get', 'List of Concept Sets');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 34);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(3, 34);

insert into ${ohdsiSchema}.sec_permission(id, value, description)	values(35, 'conceptset:*:get', 'Get conceptset expression') ;
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 35);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(3, 35);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(36, 'ir:get', 'List of incidence rates');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 36);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(37, 'ir:*:get', 'Get incidence rate');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 37);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(38, 'ir:post', 'Create new incidence rate');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 38);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(39, 'comparativecohortanalysis:get', 'List estimations');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 39);
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(40, 'comparativecohortanalysis:*:get', 'Get estimation');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 40);
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(41, 'comparativecohortanalysis:post', 'Create new estimation');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 41);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(42, 'plp:get', 'List of population level predictions');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 42);
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(43, 'plp:*:get', 'Get population level prediction');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 43);
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(44, 'plp:post', 'Create new plp');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 44);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(45, 'source:*:get', 'Read configuration');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(2, 45);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(46, 'vocabulary:*:search:*:get', 'Search vocabulary');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 46);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(47, 'cdmresults:*:get', 'View CDM results');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 47);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(48, 'cohortanalysis:get', 'Get Cohort analyses');
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(49, 'cohortanalysis:*:get', 'Get Cohort analyses summary or preview');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 48);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 49);

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


insert into ${ohdsiSchema}.sec_permission(id, value, description) values(100, 'cohortresults:*:get', 'Get Cohort reports');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 100);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(101, 'evidence:*:get', 'Get evidence');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 101);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(102, 'executionservice:*:get', 'Get Execution Service Job status');
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(103, 'executionservice:execution:run:post', 'Start Execution Service Job');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 102);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 103);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(104, 'feasibility:get', 'List of Feasibilities');
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(105, 'feasibility:*:get', 'View Feasibility');
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(106, 'feasibility:*:put', 'Edit Feasibility');
insert into ${ohdsiSchema}.sec_permission(id, value, description) values(107, 'feasibility:*:delete', 'Delete Feasibility');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 104);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 105);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 106);
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 107);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(108, 'featureextraction:*:get', 'Access to Feature Extraction');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 108);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(109, 'job:get', 'Find Job names');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 109);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(110, 'vocabulary:*:post', 'Vocabulary services');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id) values(1, 110);

insert into ${ohdsiSchema}.sec_permission(id, value, description) values(111, '*:person:*:get', 'View profiles');
insert into ${ohdsiSchema}.sec_role_permission(role_id, permission_id)	values(1, 111);

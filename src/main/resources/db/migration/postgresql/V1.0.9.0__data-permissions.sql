insert into sec_permission(id, value, description) values(34, 'conceptset:get', 'List of Concept Sets') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 34) on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(3, 34) on conflict do nothing;

insert into sec_permission(id, value, description)	values(35, 'conceptset:*:get', 'Get conceptset expression')  on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 35) on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(3, 35) on conflict do nothing;

insert into sec_permission(id, value, description) values(36, 'ir:get', 'List of incidence rates') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 36) on conflict do nothing;

insert into sec_permission(id, value, description) values(37, 'ir:*:get', 'Get incidence rate') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 37) on conflict do nothing;

insert into sec_permission(id, value, description) values(38, 'ir:post', 'Create new incidence rate') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 38) on conflict do nothing;

insert into sec_permission(id, value, description) values(39, 'comparativecohortanalysis:get', 'List estimations') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 39) on conflict do nothing;
insert into sec_permission(id, value, description) values(40, 'comparativecohortanalysis:*:get', 'Get estimation') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 40) on conflict do nothing;
insert into sec_permission(id, value, description) values(41, 'comparativecohortanalysis:post', 'Create new estimation') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 41) on conflict do nothing;

insert into sec_permission(id, value, description) values(42, 'plp:get', 'List of population level predictions') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 42) on conflict do nothing;
insert into sec_permission(id, value, description) values(43, 'plp:*:get', 'Get population level prediction') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 43) on conflict do nothing;
insert into sec_permission(id, value, description) values(44, 'plp:post', 'Create new plp') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 44) on conflict do nothing;

insert into sec_permission(id, value, description) values(45, 'source:*:get', 'Read configuration') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(2, 45) on conflict do nothing;

insert into sec_permission(id, value, description) values(46, 'vocabulary:*:search:*:get', 'Search vocabulary') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 46) on conflict do nothing;

insert into sec_permission(id, value, description) values(47, 'cdmresults:*:get', 'View CDM results') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 47) on conflict do nothing;

insert into sec_permission(id, value, description) values(48, 'cohortanalysis:get', 'Get Cohort analyses') on conflict do nothing;
insert into sec_permission(id, value, description) values(49, 'cohortanalysis:*:get', 'Get Cohort analyses summary or preview') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 48) on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 49) on conflict do nothing;

insert into sec_permission(id, value, description) values(100, 'cohortresults:*:get', 'Get Cohort reports') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 100) on conflict do nothing;

insert into sec_permission(id, value, description) values(101, 'evidence:*:get', 'Get evidence') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 101) on conflict do nothing;

insert into sec_permission(id, value, description) values(102, 'execution_service:*:get', 'Get Execution Service Job status') on conflict do nothing;
insert into sec_permission(id, value, description) values(103, 'execution_service:run_script:post', 'Start Execution Service Job') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 102) on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 103) on conflict do nothing;

insert into sec_permission(id, value, description) values(104, 'feasibility:get', 'List of Feasibilities') on conflict do nothing;
insert into sec_permission(id, value, description) values(105, 'feasibility:*:get', 'View Feasibility') on conflict do nothing;
insert into sec_permission(id, value, description) values(106, 'feasibility:*:put', 'Edit Feasibility') on conflict do nothing;
insert into sec_permission(id, value, description) values(107, 'feasibility:*:delete', 'Delete Feasibility') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 104) on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 105) on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 106) on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 107) on conflict do nothing;

insert into sec_permission(id, value, description) values(108, 'featureextraction:*:get', 'Access to Feature Extraction') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 108) on conflict do nothing;

insert into sec_permission(id, value, description) values(109, 'job:get', 'Find Job names') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 109) on conflict do nothing;

insert into sec_permission(id, value, description) values(110, 'vocabulary:*:post', 'Vocabulary services') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id) values(1, 110) on conflict do nothing;

insert into sec_permission(id, value, description) values(111, '*:person:*:get', 'View profiles') on conflict do nothing;
insert into sec_role_permission(role_id, permission_id)	values(1, 111) on conflict do nothing;

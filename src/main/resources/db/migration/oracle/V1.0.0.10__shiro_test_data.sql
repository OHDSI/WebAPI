-- Test data for Shiro demo

INSERT INTO SEC_USER VALUES (1,'admin', 'password', null , 'AdminName');
INSERT INTO SEC_USER VALUES (2,'operator', 'pass1', null , 'OperatorName');

INSERT INTO SEC_ROLE VALUES (1, 'Admin');
INSERT INTO SEC_ROLE VALUES (2, 'Operator');

INSERT INTO SEC_USER_ROLE VALUES (1,1);
INSERT INTO SEC_USER_ROLE VALUES (2,2);

INSERT INTO SEC_PERMISSION VALUES (1,'edit vocabulary', 1);
INSERT INTO SEC_PERMISSION VALUES (2,'browse vocabulary', 2);
-- Initial roles and permissions

INSERT INTO SEC_ROLE VALUES (1, 'reader');
INSERT INTO SEC_ROLE VALUES (2, 'writer');
INSERT INTO SEC_ROLE VALUES (3, 'admin');

INSERT INTO SEC_PERMISSION VALUES (1,'read', 1);
INSERT INTO SEC_PERMISSION VALUES (2,'read,create,update,delete,execute', 2);
INSERT INTO SEC_PERMISSION VALUES (3,'*', 3);
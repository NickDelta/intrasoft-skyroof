/*
This script inserts:
- 3 users - each with different role to have all options available for mocking
*/

--USERS--

INSERT INTO USER_TABLE(user_id,username,password,role,creation_date)
VALUES (1,'admin','password','ROLE_ADMIN',CURRENT_TIMESTAMP());

INSERT INTO USER_TABLE(user_id,username,password,role,creation_date)
VALUES (2,'reporter','password','ROLE_REPORTER',CURRENT_TIMESTAMP());

INSERT INTO USER_TABLE(user_id,username,password,role,creation_date)
VALUES (3,'consult','password','ROLE_CONSULT',CURRENT_TIMESTAMP());
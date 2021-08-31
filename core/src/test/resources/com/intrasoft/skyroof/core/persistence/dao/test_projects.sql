--PROJECTS--

INSERT INTO PROJECT(project_id,title,description,owner_id,creation_date,is_deleted)
VALUES (1, 'Skyroof App', 'Implement this API', 1, CURRENT_TIMESTAMP(), false);

INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (1,1);
INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (1,2);

INSERT INTO PROJECT(project_id,title,description,owner_id,creation_date,is_deleted)
VALUES (2, 'Hermes App', 'Implement this app', 1, CURRENT_TIMESTAMP(), false);

INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (2,1);
INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (2,3);

INSERT INTO PROJECT(project_id,title,description,owner_id,creation_date,is_deleted)
VALUES (3, 'Hlios App', 'Implement this app', 1, CURRENT_TIMESTAMP(), false);

INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (3,1);
INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (3,2);
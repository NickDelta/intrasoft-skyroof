--PROJECTS--

INSERT INTO PROJECT(project_id,title,description,owner_id,creation_date,is_deleted)
VALUES (1, 'Title 1', 'Description 1', 1, CURRENT_TIMESTAMP(), false);

INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (1,1);
INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (1,2);

INSERT INTO PROJECT(project_id,title,description,owner_id,creation_date,is_deleted)
VALUES (2, 'Title 2', 'Description 2', 1, CURRENT_TIMESTAMP(), false);

INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (2,1);
INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (2,3);

INSERT INTO PROJECT(project_id,title,description,owner_id,creation_date,is_deleted)
VALUES (3, 'Title 3', 'Description 3', 1, CURRENT_TIMESTAMP(), false);

INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (3,1);
INSERT INTO PROJECT_COLLABORATORS(fk_project, fk_user) VALUES (3,2);
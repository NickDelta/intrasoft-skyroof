# Skyroof Spring Application

## Data Model

A client wants to monitor this construction company projects and the tasks that are needed for the project to be completed. 

- Each task can have a state: 
  - NOT STARTED
  - IN PROGRESS 
  - COMPLETED
    
- Each project will have the attributes: 
  - Id
  - Title
  - Description
  - Creation Date
    
- Each task will have the attributes:
  - Id
  - Project Id 
  - Title 
  - Description 
  - State 
  - Creation Date
  - Start Date
  - Completed Date
---
## Projects Business Logic
A project can have many tasks, a task can only be assigned to a single project. A task cannot be assigned to a different project upon creation.
The system should be able to calculate a project state (on request and not persisted on the db):
- NOT STARTED when all tasks are in NOT STARTED state
- IN PROGRESS when at least one task is in status IN PROGRESS
- COMPLETED when all tasks are in status COMPLETED
- (My assumption) There is an edge case where some tasks can have NOT_STARTED state and some others COMPLETED state. A project is considered to be IN_PROGRESS in that case.
- (My assumption) If a project does not have any tasks yet, it is considered NOT_STARTED.

---

## Delete Operations Business Logic
- A task can be deleted **only** when it is in state NOT STARTED.
- A project can be deleted **only** when all tasks are in state DELETED.

Nothing is actually deleted from the system. It is only marked as DELETED. Deleted items cannot be modified and are not calculated.

---

## State Transitions on Tasks

- The most normal transition is NOT_STARTED -> IN_PROGRESS -> COMPLETED.
- It is possible for a task to transit from COMPLETED to IN_PROGRESS state. In that
  case, the completion date becomes null again and start_date gets a new date.
- It is not allowed for a task to transit from IN_PROGRESS or COMPLETED 
  states to NOT_STARTED state.
- Tasks cannot be undeleted once in state DELETED.

---

## Security

The only unauthenticated endpoints are:

- POST /users/signup where users can signup for an account.
- POST /users/login where users can receive a Bearer token to make requests to authenticated endpoints.

There are 3 roles that a user can have:

- ROLE_CONSULT : Permission to read projects and tasks where the user is a collaborator.
- ROLE_REPORTER : Permission to read/edit projects and tasks where the user is a collaborator.
- ROLE_ADMIN : Permission to read/edit/delete projects and tasks where the user is a collaborator.


- Only admins can create new projects. Tasks can be created by reporters or admins.
- Collaborators can be added or removed only by admins. For technical reasons, 
  the owner of the project is also considered an collaborator. Owners cannot remove themselves
  from collaborators.
---

## Deploy app to Weblogic

1. Fire up Oracle DB & Oracle Weblogic:
```bash
docker-compose -f docker-compose-weblogic.yaml up
```
Wait until both services are ready.

2. Execute the following maven goal:
```bash
mvn pre-integration-test -Pweblogic
```
---

## Deploy app as a container (Embedded Tomcat)

1. Execute the following maven goal:
```bash
mvn package
```

2. Fire up Oracle DB & Create the container:
```bash
docker-compose -f docker-compose-embedded.yaml up
```
---

## Tasks

### Requirements

1. Implement the task JPA entity and add the 1-* constraint between project-task. (✔️)
2. Enhance the REST API to perform all crud operations on project and task entities. (✔️)
3. Implement the business validations and state transition on entities. User-friendly messages must be returned on exceptions. (✔️)
4. Currently, the system is connected to an H2 on-memory database. Connect with an Oracle Database 19c Edition instance and provide the DDL script that creates the schema. (✔️)
5. Add logging on important functionality (new project/task, status change etc). Logs will be saved in files. (✔️)
6. Perform unit tests on: 
   - Data Layer (✔️)
   - Business (Service) Layer (✔️)
   - Controller Layer (✔️)
   
 
### Bonus

1. Containerize the application (Docker file jar + sql db)(✔️)
2. Apply user access security per client with roles (Read, Write). Use OAuth2, JWT implementation, Spring security or any other security framework of your choice. (✔️)

#### Advanced
1. Split the maven modules horizontally (DATA layers, Business Layer, WEB layer)(✔️)
2. Nothing is actually is deleted from the system. It is only marked as DELETED. Deleted items cannot be modified and are not calculated. (✔️)
3. Keep state transition auditing (using Hibernate Envers) (✔️)
4. Implement a GitHub like scenario where project owners can invite other collaborators to work on their projects.
   Users that are not collaborators will not have access to the projects or tasks associated with them.  (✔️)
5. Throttling (5 calls/10 sec per IP) using Bucket4j (✔️)

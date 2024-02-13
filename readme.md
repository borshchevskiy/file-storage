# File Storage

Cloud file storage. Users can upload and store their files.

## Component diagram

Main components of the application are shown on the diagram.

![component_diagram.png](docs%2Fcomponent_diagram%2Fcomponent_diagram.png)

Core of the app is Backend component which is Spring Boot web app. It communicates with Frontend (JavaScript),
Database (PostgresSQl), Cache (Redis), and Storage (MinIO).

Frontend (JavaScript) component handles some request processing on client side and web-page content manipulation.

Cache (Redis) component is used for storing user's sessions.

Database (PostgresSQL) component stores users and their roles.

Storage (MinIO) is used for storing user files.
For more info on MinIO see: https://min.io/

## Class diagram

User of the app is represented by User entity and is stored in database along with its Role.

Classes:

![User.jpg](docs%2Fclass_diagram%2FUser.jpg)![Role.jpg](docs%2Fclass_diagram%2FRole.jpg)

Database relation:

![user_database.jpg](docs%2Fclass_diagram%2Fuser_database.jpg)

Another objects of interaction are files, which are stored in Storage. 
Class FileItemDto represents file information (only metadata, not file contents), 
and is used for file representation between components of the application.

Class:

![FileItem.jpg](docs%2Fclass_diagram%2FFileItem.jpg)


## Sequence diagram

Sequence diagrams of main operations with files and directories:

![files and directories actions](docs%2Fsequence_diagrams%2Ffiles%20and%20directories%20actions.png)

![file download](docs%2Fsequence_diagrams%2Ffile%20download.png)
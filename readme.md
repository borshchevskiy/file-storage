# File Storage

Cloud file storage. Users can upload and store their files.

## How to run with Docker

<b>Requirements:</b> 
- [Docker](https://docs.docker.com/get-docker/)

Navigate to project's folder and run command: 
```shell
docker compose up
```

## How to run within IDE

<b>Requirements:</b> 
- [JDK17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Maven 3](https://maven.apache.org/download.cgi)

You need to start related services first - Postgres, Redis and MinIO.
Easiest way to do this is to run them in docker containers.

<b>Postgres:</b> 
```shell
docker run --name some_postgres_name -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=file_storage -d postgres:15.3
```
After that create a schema `'file_storage'` in `'file_storage'` database. 
You can use SQL script from project folder `imports/db/02-init-schema.sql`.

For more info on PostgreSQL see: https://www.postgresql.org/

<b>Redis:</b> 
```shell
docker run --name some_redis_name -p 6379:6379 -d redis:7.0
```
For more info on Redis see: https://redis.io/

<b>MinIO:</b> 
```shell
docker run --name some_minio_name  -p 9000:9000 -p 9001:9001 -e MINIO_ROOT_USER=MINIO -e MINIO_ROOT_PASSWORD=MINIOMINIO -v C:\minio\data:/data quay.io/minio/minio server /data --console-address ":9001"
```
Pay attention to `-v` argument which sets path to storage on <b>your</b> machine. Example given is for Windows.
Change this path according to your OS and preferences (for example for Linux you may use `-v ~/minio/data:/data`).

For more info on MinIO see https://min.io/

You can download, install and run all these services manually using the links given. 
All env variables you may require are obvious from docker commands listed before.

After all services are up and running you can execute the `main` method in `ru.borshchevskiy.filestorage.FileStorageApplication`.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:
```shell
mvn spring-boot:run
```

## Component diagram

Main components of the application are shown on the diagram.

![component_diagram.png](docs%2Fcomponent_diagram%2Fcomponent_diagram.png)

Core of the app is Backend component which is Spring Boot web app. It communicates with Frontend (JavaScript),
Database (PostgresSQL), Cache (Redis), and Storage (MinIO).

Frontend (JavaScript) component handles some request processing on client side and web-page content manipulation.

Frontend and Backend components separation is relative. They are parts of one project, and they are not distributed
in separate modules or services. They are just JavaScript scripts which can be consolidated as Frontend component
and Spring Boot app which is defined as Backend component.

Cache, Database and Storage components are separate applications.

Cache (Redis) component is used for storing user's sessions.

Database (PostgreSQL) component stores users and their roles.

Storage (MinIO) is used for storing user files.

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
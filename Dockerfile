#
# Build stage
#
FROM maven:3.9.0-eclipse-temurin-17-alpine AS build
WORKDIR /file-storage-app
ADD pom.xml .
RUN mvn verify --fail-never
COPY . .
RUN ["mvn", "package", "-Dmaven.test.skip=true"]

#
# Package stage
#
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /file-storage-app
COPY --from=build /file-storage-app/target/*.jar *.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "*.jar" ]
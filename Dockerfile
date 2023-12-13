#
# Build stage
#
FROM maven:3.9.0-eclipse-temurin-17-alpine AS build
COPY . .
RUN mvn clean install

#
# Package stage
#
FROM eclipse-temurin:17-jdk-alpine
COPY --from=build /target/*.jar *.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "*.jar" ]
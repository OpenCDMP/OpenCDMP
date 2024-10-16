####################################### Build stage #######################################
FROM maven:3.9-eclipse-temurin-21-alpine AS build-stage

ARG PROFILE

COPY pom.xml /build/
COPY core /build/core/
COPY web /build/web/
RUN rm -f /build/web/src/main/resources/config/*-devel.yml
RUN rm -f /build/web/src/main/resources/logging/*.xml

WORKDIR /build/
RUN mvn dependency:go-offline
RUN mvn clean package
######################################## Run Stage ########################################
FROM eclipse-temurin:21

ARG PROFILE
ENV SERVER_PORT=8080
EXPOSE ${SERVER_PORT}

COPY --from=build-stage /build/web/target/web-1.0.0-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=${PROFILE}", "-Dspring.config.additional-location=file:/config/", "-jar", "/app/app.jar"]
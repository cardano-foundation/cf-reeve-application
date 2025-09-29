FROM openjdk:21-jdk-slim AS build

ARG GITLAB_MAVEN_REGISTRY_URL
WORKDIR /app
COPY . /app

RUN ./gradlew clean -x test build

FROM openjdk:21-jdk-slim AS backend
COPY --from=build /app/cf-application/build/libs/*-all.jar /app.jar
ENTRYPOINT ["java", "--enable-preview", "-jar", "/app.jar"]

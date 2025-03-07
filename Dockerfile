# I will update the PR Hash before merging. I added this tag just for the sake of testing the pipeline.
ARG LIB_DOCKER_IMAGE=pro.registry.gitlab.metadata.dev.cf-deployments.org/base-infrastructure/docker-registry/cf-lob-platform-library-m2-cache:PR178-ed8f095-GHRUN13717071467

FROM ${LIB_DOCKER_IMAGE} AS m2-cache

FROM openjdk:21-jdk-slim AS build
WORKDIR /app
COPY . /app

COPY --from=m2-cache /root/.m2 /root/.m2

RUN ./gradlew clean -x test build

FROM openjdk:21-jdk-slim AS backend
COPY --from=build /app/cf-application/build/libs/*SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "--enable-preview", "-jar", "/app.jar"]

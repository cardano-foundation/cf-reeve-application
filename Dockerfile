ARG LIB_DOCKER_IMAGE=pro.registry.gitlab.metadata.dev.cf-deployments.org/base-infrastructure/docker-registry/cf-reeve-platform-library-m2-cache:PR260-bd33d27-GHRUN15017394798
FROM ${LIB_DOCKER_IMAGE} AS m2-cache

FROM openjdk:21-jdk-slim AS build
WORKDIR /app
COPY . /app

COPY --from=m2-cache /root/.m2 /root/.m2

RUN ./gradlew clean -x test build

FROM openjdk:21-jdk-slim AS backend
COPY --from=build /app/cf-application/build/libs/*SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "--enable-preview", "-jar", "/app.jar"]

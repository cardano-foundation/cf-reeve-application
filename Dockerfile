FROM eclipse-temurin:21 as build

ARG GITLAB_MAVEN_REGISTRY_URL
WORKDIR /app
COPY . /app

RUN ./gradlew clean -x test build

FROM eclipse-temurin:21-jre AS backend
COPY --from=build /app/cf-application/build/libs/*-all.jar /app.jar
# Install libsodium system library so lazysodium can load it
RUN apt-get update && apt-get install -y libsodium23 && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["java", "--enable-preview", "-jar", "/app.jar"]

FROM openjdk:21-jdk-slim AS build
WORKDIR /app
COPY . /app

RUN ./gradlew clean -x test build

FROM openjdk:21-jdk-slim AS backend
COPY --from=build /app/cf-application/build/libs/*SNAPSHOT.jar /app.jar
ENTRYPOINT ["java", "--enable-preview", "-jar", "/app.jar"]

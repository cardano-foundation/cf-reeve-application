FROM dhi.io/eclipse-temurin:21-jdk-debian-dev AS build

ARG GITLAB_MAVEN_REGISTRY_URL
WORKDIR /app
COPY . /app

# The development image has apt; export only the runtime shared object.
RUN apt-get update \
 && apt-get install --no-install-recommends -y libsodium23 \
 && rm -rf /var/lib/apt/lists/* \
 && library="$(find /usr/lib -type f -name 'libsodium.so.*' -print -quit)" \
 && test -n "$library" \
 && install -Dm755 "$library" "/out/opt/libsodium/$(basename "$library")" \
 && ln -s "$(basename "$library")" /out/opt/libsodium/libsodium.so

RUN ./gradlew clean -x test build -PlocalRepo=/app/.m2/repository

FROM dhi.io/eclipse-temurin:21-debian AS backend
COPY --from=build /app/cf-application/build/libs/*-all.jar /app.jar
COPY --from=build /app/cf-application/build/libs/healthcheck.jar /healthcheck.jar
COPY --from=build /out/ /

ENTRYPOINT ["java", "-Djna.library.path=/opt/libsodium", "--enable-preview", "-jar", "/app.jar"]

# Introduction
CF specific application that uses cf-lob-platform

# Requirements
- Linux or OSX
- JDK 21 LTS installed

# Developer Start
Make sure you have JDK 21 LTS properly configured. Here is an example using Amazon Corretto flavour.

```
mati@MacBook-Pro cf-lob-platform % java --version
openjdk 21 2023-09-19 LTS
OpenJDK Runtime Environment Corretto-21.0.0.35.1 (build 21+35-LTS)
OpenJDK 64-Bit Server VM Corretto-21.0.0.35.1 (build 21+35-LTS, mixed mode, sharing)
```

```bash
git clone https://github.com/cardano-foundation/cf-lob-platform
cd cf-lob-platform
./gradlew clean build publishMavenJavaPublicationToLocalM2Repository
```
once it succeeds:

BUILD SUCCESSFUL in 13s
49 actionable tasks: 49 executed

Starting postgres DB locally
```bash
git clone https://github.com/cardano-foundation/cf-lob
docker compose up
```

```bash
export SPRING_PROFILES_ACTIVE=dev--yaci-dev-kit
export SPRING_CONFIG_LOCATIONS=classpath:/application.yml,classpath:/application-dev--yaci-dev-kit.yml;export SPRING_PROFILES_ACTIVE=dev--yaci-dev-kit

./gradlew clean bootRun
```

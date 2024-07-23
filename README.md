# Introduction
CF specific application that uses cf-lob-platform

# Developer Start
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

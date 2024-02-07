#!/bin/sh
export SPRING_CONFIG_LOCATIONS=SPRING_CONFIG_LOCATIONS=classpath:/application.yml,classpath:/application-dev--yaci-dev-kit.yml
export SPRING_PROFILES_ACTIVE=dev--yaci-dev-kit

docker-compose down
./gradlew clean bootRun


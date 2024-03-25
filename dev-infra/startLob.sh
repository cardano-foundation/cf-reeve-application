#!/bin/sh
export SPRING_CONFIG_LOCATIONS=classpath:/application.yml,classpath:/application-dev--yaci-dev-kit.yml
export SPRING_PROFILES_ACTIVE=dev--yaci-dev-kit

if [ -x "$(command -v docker-compose)" ]; then
    echo "SUCCESS: docker-compose (v1) is installed."
    docker-compose down
    ./gradlew clean bootRun
    exit 0
fi

if $(docker compose &>/dev/null) && [ $? -eq 0 ]; then
    echo "SUCCESS: docker compose (v2) is installed."
    docker compose down
    ./gradlew clean bootRun
    exit 0
fi

echo "ERROR: neither \"docker-compose\" nor \"docker compose\" appear to be installed."
exit 1



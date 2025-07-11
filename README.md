# Reeve | Cardano Foundation
In a time where trust and transparency hold paramount importance, blockchain emerges as a powerful solution, enabling a fundamental transformation in financial data management.
Reeve, also known as Ledger on the Blockchain (LOB), empowers organizations to securely record and share critical financial data on the Cardano blockchain, ensuring its integrity and verifiability for all stakeholders.

## Project Overview
The Reeve platform is designed to integrate traditional accounting systems with blockchain technology, ensuring transparency, immutability, and enhanced security for financial records. By leveraging the Cardano blockchain, the platform offers a decentralized approach to financial data management, facilitating trust and efficiency in accounting processes.

Learn more about Reeve: [Reeve - Ledger on the Blockchain](https://www.cardanofoundation.org/blog/boosting-transparency-on-chain-financial-report)


This application is an implementation of the [cf-reeve-platform](https://github.com/cardano-foundation/cf-reeve-platform). 
Please find all further information in this repository.

This repository provides the needed configuration to start the whole application stack and use the Reeve platform. 

# How to start the application
To start the whole application stack you need to run the following command in this repository root:
```bash
docker compose --profile frontend up -d --build
```
The `--build` flag is optional and is used to rebuild the images if you have made changes to the Dockerfiles.
This will start the following services:
- `cf-lob-frontend`: The frontend application
- `cf-lob-api`: The modules `accounting_core` and `organisation` of the cf-lob-platform
- `cf-lob-publisher`: The module `blockchain_publisher`, `erp_adapter` and `blockchain_reader` of the cf-lob-platform
- `cf-ledger-follower`: The submodule `ledger_follower` of the cf-lob-platform connected to a `yaci-devkit`
- `postgres`: The database used by the cf-lob-platform
- `yaci-devkit`: The Yaci DevKit used by the `cf-ledger-follower` to follow the ledger, a single node cardano dev blockchain
- `yaci-viewer`: The frontend to access the Yaci DevKit like an explorer
- `keycloak`: The identity provider used by the cf-lob-platform
- `kafka`: The message broker used by the cf-lob-platform
- `zookeeper`: The coordination service used by the message broker

## How to develop
Start the application stack as described above. Then you can stop containers you are currently working on.
An example you are working on the `cf-lob-platform` repository and the `accounting_reporting_core` module:
```bash
sudo vim /etc/hosts # add the following line: 127.0.0.1 kafka (I didn't found a better way to do this yet, this needs to be done only once)
dockker compose up -d --build # to start the whole stack
docker container stop api # to stop the api container
## within cf-lob-platform repository 
./gradlew clean build publishMavenJavaPublicationToLocalM2Repository # to build the module and publish the artifacts to your local m2 repository
cp cf-application/.env.template cf-application/.env # to copy the .env file
## adjust your .env file to your preferences
# ensure the dev profile is used
export SPRING_PROFILES_ACTIVE=dev--yaci-dev-kit
./gradlew clean bootRun # to start the api container
```

### Things to tweak if needed
- Keycloak can be disabled by setting `KEYCLOAK_ENABLED=false` in the `.env` file or in the docker-compose file 
- To test the api swagger set the following two environment variables in the `.env` file or in the docker-compose file:
  - `KEYCLOAK_ENABLED=false`
  - `KC_BASE_URL=http://localhost:8080`
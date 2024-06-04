<div align="center">
  <hr />
  <h1 align="center" style="border-bottom: none">Cardano Foundation | Ledger on the Blockchain</h1>

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)]
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-%23FE5196?logo=conventionalcommits&logoColor=white)](https://conventionalcommits.org)
![GitHub release (with filter)](https://img.shields.io/github/v/release/cardano-foundation/cf-lob)
![Discord](https://img.shields.io/discord/1022471509173882950)

[![Build](https://github.com/cardano-foundation/cf-lob/actions/workflows/build.yml/badge.svg)](https://github.com/cardano-foundation/cf-lob/actions/workflows/build.yml)
[![Build with tests](https://github.com/cardano-foundation/cf-lob/actions/workflows/build.yml/badge.svg)](https://github.com/cardano-foundation/cf-lob/actions/workflows/build-with-tests.yaml)

<hr />
</div>

The Ledger on the Blockchain (LOB) project aims to develop a solution that supports the adoption of Blockchain as a decentralised ledger, for digital recording and storing of accounting and financial information, by developing interface applications (APIs) that will execute the reading, conversion and validation of data across the different phases of the process.

For the project adopters, it will provide the opportunity to advance the use of the blockchain  technology to share the organisation’s financial information in a secure, transparent, efficient and potentially low-cost way, at the same time that opens up new chances to improve, optimise and automate internal business processes.

## Quickstart

Prerequisties:
- Java 21
- 100GB of disk space
- 10GB of RAM

## How to build

```
git clone git@github.com:cardano-foundation/cf-lob.git
cd cf-lob
./gradlew clean build
```

## How to run locally
#### Docker:
```shell
# start the containers and run the command
docker compose up --build -d
docker exec -it app ./gradlew clean build
```

## How to run

```
git clone git@github.com:cardano-foundation/cf-lob.git
cd cf-lob
export SPRING_CONFIG_LOCATIONS=classpath:/application.yml,classpath:/application-dev--yaci-dev-kit.yml
export SPRING_PROFILES_ACTIVE=dev--yaci-dev-kit
./gradlew clean bootRun
```

## Architecture
#### Docker:
```shell
docker compose up --build -d
docker exec -it app ./gradlew bootRun
```
## How to test

<img src="https://github.com/cardano-foundation/cf-lob/LOB_-_Arch.jpg" />

## Documentation

| Link                                                                               | Audience                                                     |
|------------------------------------------------------------------------------------|--------------------------------------------------------------|
| [Code Of Conduct](https://github.com/cardano-foundation/cf-lob/CODE-OF-CONDUCT.md) | Developers                                                   |
| [Contributing](https://github.com/cardano-foundation/cf-lob/CONTRIBUTING.md)       | Developers                                                   |
| [Security](https://github.com/cardano-foundation/cf-lob/SECURITY.md)               | Developers                                                   |

<hr/>

<p align="center">
  <a href="https://github.com/cardano-foundation/cardano-wallet/blob/master/LICENSE"><img src="https://img.shields.io/github/license/cardano-foundation/cardano-wallet.svg?style=for-the-badge" /></a>
</p>

# Ledger on the Blockchain (LoB)

<p align="left">
<img alt="Tests" src="https://github.com/cardano-foundation/cf-explorer-api/actions/workflows/tests.yaml/badge.svg" />
</p>

The Ledger on the Blockchain (LOB) project aims to develop a solution that supports the adoption of Blockchain as a decentralised ledger, for digital recording and storing of accounting and financial information, by developing interface applications (APIs) that will execute the reading, conversion and validation of data across the different phases of the process.

For the project adopters, it will provide the opportunity to advance the use of the blockchain  technology to share the organisation’s financial information in a secure, transparent, efficient and potentially low-cost way, at the same time that opens up new chances to improve, optimise and automate internal business processes.

## Architecture Overview
![LOB-Overview drawio](https://github.com/cardano-foundation/cf-lob/assets/2879295/c1d7339a-c333-4998-a487-07273d2ac610)


## Quickstart


The `lob` executable is an HTTP server that manages...:

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
./gradlew clean bootRun
```

#### Docker:
```shell
docker compose up --build -d
docker exec -it app ./gradlew bootRun
```
## How to test

Test instructions for individual modules are provided in the corresponding sub folder README files.

System level integration test instructions go here: TBD


## Documentation

| Link                                                                                             | Audience                                                     |
|--------------------------------------------------------------------------------------------------| ------------------------------------------------------------ |
| [Documentation](https://github.com/cardano-foundation/cf-lob/)                                   |                                                              |
| • [User Manual](https://github.com/cardano-foundation/cf-lob/)                                   | Users of LoB                                      |
| • [Contributor Manual](https://cardano-foundation.github.io/cardano-wallet/contributor)          | Anyone interested in the project and our development process |

<hr/>

<p align="center">
  <a href="https://github.com/cardano-foundation/cardano-wallet/blob/master/LICENSE"><img src="https://img.shields.io/github/license/cardano-foundation/cardano-wallet.svg?style=for-the-badge" /> or MPL2.0</a>
</p>
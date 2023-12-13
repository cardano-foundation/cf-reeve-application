# Ledger on the Blockchain

<p align="left">
<img alt="Tests" src="https://github.com/cardano-foundation/cf-explorer-api/actions/workflows/tests.yaml/badge.svg" />
</p>

The Ledger on the Blockchain (LOB) project aims to develop a solution that supports the adoption of Blockchain as a decentralised ledger, for digital recording and storing of accounting and financial information, by developing interface applications (APIs) that will execute the reading, conversion and validation of data across the different phases of the process.

For the project adopters, it will provide the opportunity to advance the use of the blockchain  technology to share the organisation’s financial information in a secure, transparent, efficient and potentially low-cost way, at the same time that opens up new chances to improve, optimise and automate internal business processes.

## Architecture Overview
![LOB-Overview drawio](https://github.com/cardano-foundation/cf-lob/assets/2879295/c1d7339a-c333-4998-a487-07273d2ac610)

## How to build

```
git clone git@github.com:cardano-foundation/cf-lob.git
cd cf-lob
mvn clean package
```

## How to test

Test instructions for individual modules are provided in the corresponding sub folder README files.

System level integration test instructions go here: TBD

***
#### ToDelete
```
curl --location 'http://localhost:8082/netsuite/client'

curl --location 'http://localhost:8081/events/registrations/approve' --header 'Content-Type: application/json' --data '{"registrationId": "ad9f70bb952de9be813e1abf06dddba3681b8d9ca80692b77e11e99612c4487c"}';

for este in {0..10}; do curl http://localhost:8081/events/tx/resubmit/$este; done;clear;
```
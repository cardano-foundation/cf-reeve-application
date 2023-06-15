# Ledger on the Blockchain - TxSubmitter

The purpose of the TxSubmitter module is to consume submit jobs from the core event stream, create according data objects that shall be stored as blockchain transaction metadata, manage a set of UTXOs for a configured address and eventually submit and monitor the submission process of transactions. It has shared access to an ODS that is shared among the core microservices considered as streaming sinks or processors.

## How to build

```
git clone git@github.com:cardano-foundation/cf-lob.git
cd cf-lob
mvn clean package
```

## How to test

- execute unit tests via mvn test
- execute integration tests TBD

# Ledger on the Blockchain - Chain Data Event Consumer

The purpose of the Chain Data Event Consumer is to process chain data events and consolidate the information needed by other core modules in the core ODS. It therefore acts as a LedgerSync consumer.

## How to build

```
git clone git@github.com:cardano-foundation/cf-lob.git
cd cf-lob
mvn clean package
```

## How to test

- execute unit tests via mvn test
- execute integration tests TBD
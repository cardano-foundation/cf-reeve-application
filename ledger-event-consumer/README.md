# Ledger on the Blockchain - Ledger Event Consumer

The Ledger Event Consumer module is a data processer that consumes ledger events from the core event stream, persists relevant information in the core ODS and submits transaction submit jobs back to the core event stream.

## How to build

```
git clone git@github.com:cardano-foundation/cf-lob.git
cd cf-lob
mvn clean package
```

## How to test

- execute unit tests via mvn test
- execute integration tests TBD
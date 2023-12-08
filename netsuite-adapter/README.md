# Ledger on the Blockchain - Netsuite Source Adapter

The Netsuite Source Adapter implements a module that interfaces with the available technical interfaces of a Netsuite instance to fetch relevant ledger information and forwards it to the Source API module for further downstream processing in the LOB system.

## How to build

```
git clone git@github.com:cardano-foundation/cf-lob.git
cd cf-lob
mvn clean package
```

## How to test

- execute unit tests via mvn test
- execute integration tests in sandbox env TBD
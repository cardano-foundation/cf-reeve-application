# Ledger on the Blockchain - Admin API

The Admin API module exposes a REST API interface which allows other modules to submit configuration updates which are transformed and published as config events to the core event stream for further processing by other core modules.

## How to build

```
git clone git@github.com:cardano-foundation/cf-lob.git
cd cf-lob
mvn clean package
```

## How to test

- execute unit tests via mvn test
- execute integration tests TBD
- Postman
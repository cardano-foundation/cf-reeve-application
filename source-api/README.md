# Ledger on the Blockchain - Source API

The purpose of the Source API is to allow source system adapter modules to submit ledger events which get published to the core event stream. It leverages Keycloak for Access and Identity Management functions. The Source API module itself is stateless.

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

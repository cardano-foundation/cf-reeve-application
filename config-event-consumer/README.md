# Ledger on the Blockchain - Config Event Consumer

The Config Event Consumer module is a data processor that processes config events from the core event stream and persists them in the core ODS to make the config data available to other core modules. On system start up it also reads the initially deployed configuration and publishes it back to the core event stream for the Webview API module to consume so that it is aware of the bootstrap configuration.

## How to build

```
git clone git@github.com:cardano-foundation/cf-lob.git
cd cf-lob
mvn clean package
```

## How to test

- execute unit tests via mvn test
- execute integration tests TBD
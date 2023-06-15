# Ledger on the Blockchain - Webview API

The purpose of the Webview API module is to serve out relevant information to the LOB webview frontend application via a REST API and forward and fetch information from services in the core layer. The Webview API service uses the authentication and access management service (Keycloak) to verify user requests and authenticates itself to any other core microservices using the same method. It has read only access to the core event stream to consume config update events and observe the inner status of the core services. The consumed data is consolidated in its own ODS. It issues configuration updates via the Admin API microservice.

## How to build

```
git clone git@github.com:cardano-foundation/cf-lob.git
cd cf-lob
mvn clean package
```

## How to test

- execute unit tests via mvn test
- execute integration tests TBD
- Postman collection

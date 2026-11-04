# Manual Keycloak configuration with keycloak-config-cli

This directory contains focused, declarative configuration for applying Reeve
changes to an existing Keycloak realm. It is **not** a full realm import and is
not wired into Docker Compose.

## What the migrations change

[`001-reeve-api-resource-server.json`](001-reeve-api-resource-server.json):

- creates or reconciles the bearer-only `reeve-api` client;
- reconciles the existing `roles` client scope;
- adds the `reeve-api` audience mapper;
- adds the flat `roles` claim mapper used by Spring Security.

[`002-basic-client-scope.json`](002-basic-client-scope.json):

- creates or reconciles the `basic` client scope;
- adds the `sub` access-token mapper;
- adds the `auth_time` ID- and access-token mapper;
- assigns `basic` as a default scope of `webclient`.

The full existing `protocolMappers` list is declared for each managed scope
because keycloak-config-cli treats that list as desired state. Omitting an
existing mapper would remove it. Other properties omitted from a managed
resource are preserved. Review the target realm before the first production run
if either scope has environment-specific mappers that are not represented here.

Only `clientId` and `defaultClientScopes` are declared for `webclient`, so its
other properties are preserved. The complete default-scope list from the
checked-in realm is included because that list is also desired state; any
environment-specific default scopes not represented in the migration would be
removed.

## Apply manually with Docker

Run from the repository root while Keycloak is running. The following command
uses a pinned keycloak-config-cli build for Keycloak 26 and connects to the
Keycloak container through the existing `lob` Docker network:

```bash
read -r -s -p 'Keycloak admin password: ' KEYCLOAK_PASSWORD && echo
export KEYCLOAK_PASSWORD

docker run --rm \
  --network lob \
  -e KEYCLOAK_URL=http://keycloak:8080 \
  -e KEYCLOAK_USER=admin \
  -e KEYCLOAK_PASSWORD \
  -e KEYCLOAK_LOGINREALM=master \
  -e KEYCLOAK_AVAILABILITYCHECK_ENABLED=true \
  -e KEYCLOAK_AVAILABILITYCHECK_TIMEOUT=120s \
  -e 'IMPORT_FILES_LOCATIONS=/config/*.json' \
  -e IMPORT_VALIDATE=true \
  -e IMPORT_CACHE_ENABLED=false \
  -e IMPORT_REMOTESTATE_ENABLED=true \
  -e IMPORT_MANAGED_CLIENT=NO_DELETE \
  -e IMPORT_MANAGED_CLIENTSCOPE=NO_DELETE \
  -e IMPORT_MANAGED_CLIENTSCOPEMAPPING=NO_DELETE \
  -v "$PWD/keycloak-migrations/keycloak-config-cli:/config:ro" \
  adorsys/keycloak-config-cli:6.5.1-26.0.5

unset KEYCLOAK_PASSWORD
```

Adjust these values when applying to another environment:

- `--network lob`: Docker network containing Keycloak;
- `KEYCLOAK_URL`: URL reachable **from the CLI container**;
- `KEYCLOAK_USER` and `KEYCLOAK_LOGINREALM`: administrative login;
- image tag: keep the keycloak-config-cli build aligned with the Keycloak major
  version.

For an externally reachable Keycloak, omit `--network lob` and set
`KEYCLOAK_URL` to its HTTPS URL. Do not commit a password or client secret.

## Safety settings

The manual command intentionally uses:

- `IMPORT_CACHE_ENABLED=false` so every invocation reconciles current state;
- `IMPORT_REMOTESTATE_ENABLED=true` so CLI-managed state is tracked in the
  realm;
- `no-delete` for clients, client scopes, and client-scope mappings so unrelated
  resources are not purged.

`no-delete` prevents deletion of unrelated clients/scopes, but the properties
of resources explicitly named by these files are still reconciled. In
particular, the `roles.protocolMappers`, `basic.protocolMappers`, and
`webclient.defaultClientScopes` lists are authoritative.

Apply and verify in a non-production environment first, and back up Keycloak
before the first production use.

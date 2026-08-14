# Netsuite Configuration

NetSuite credentials are configured **per organisation, in the database**, through the admin UI —
not through environment variables. `LOB_NETSUITE_CLIENT_URL`, `_TOKEN_URL`, `_CLIENT_ID`,
`_CERTIFICATE_ID` and `_PRIVATE_KEY_FILE_PATH` no longer exist, and the private-key volume mount has
been removed from the compose files.

Two things still belong to the operator rather than the tenant:

- `LOB_NETSUITE_CLIENT_RECORDSPERCALL` and `LOB_NETSUITE_CLIENT_TIMEOUT_SECONDS` — request tuning.
- `LOB_CONFIG_ENCRYPTION_KEY` — see below. **Required.**

## Encryption key

Every stored private key is encrypted with AES-256-GCM before it leaves the organisation module, so
the database, the Kafka topic and any backup contain ciphertext only.

Generate the key once per environment:

```bash
openssl rand -base64 32
```

Set it as `LOB_CONFIG_ENCRYPTION_KEY` on **every service running the organisation or the netsuite
module** — in the default compose topology that means both `api` (which encrypts) and `publisher`
(which decrypts). The value must be identical on all of them.

The application refuses to start if the key is missing or does not decode to exactly 32 bytes. That
is deliberate: a service that started without it would fail later, at ingestion time, instead of at
boot.

> **Changing this key makes every stored NetSuite configuration permanently undecryptable.** There
> is no rotation tooling. If it must change, every organisation has to re-enter its credentials.

## How to set up oAuth2

This obtains the credentials for one organisation. Repeat per organisation — each can point at a
different NetSuite account.

1. Creating the certificate

To create a certificate the following command is needed
```bash
openssl req -new -x509 -newkey rsa:4096 -keyout private.pem -sigopt rsa_padding_mode:pss -sha256 -sigopt rsa_pss_saltlen:64 -out public.pem -nodes -days 365
```
This will create a certificate pair with RSA-PSS. The public part needs to be uploaded to Netsuite.
The private part must be kept secret and is pasted into the admin form (see below) — the application
no longer reads it from disk.

2. Adding the certificate to Netsuite
- Create a new integration record in Netsuite
  - Go to Setup > Integration > Manage Integrations > New
  - When created you will see a **client ID** — you need it for the form below.
  - **Attention**: You will see this clientID only once in Netsuite!
- Create Client Credential Setup (M2M)
  - Go to Setup > Integration > OAuth 2.0 Client Credentials Setup and click Create new
  - Choose an `Entity` and a `Role` and for `Application` choose the Integration you created in the previous step
  - Then Upload the public part of your certificate
  - After saving you will see a new row in the table. Copy the **certificate ID**.
3. Additional values needed:
- **Base URL** of the Netsuite account (usually `https://<NETSUITE_ID>.restlets.api.netsuite.com/app/site/hosting/restlet.nl?<EXTRA PARAMS>`)
- **Token URL** of the Netsuite account (usually `https://<NETSUITE_ID>.suitetalk.api.netsuite.com/services/rest/auth/oauth2/v1/token`)

## Entering the configuration

In the frontend go to **Settings → NetSuite Configuration** (visible to `reeve_admin` only) and
supply the base URL, token URL, client ID, certificate ID and the contents of `private.pem`.

Or call the API directly:

```bash
curl -X POST "$API/api/v1/organisations/$ORG_ID/netsuite-configuration" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
        "baseUrl":       "https://1234567.restlets.api.netsuite.com/app/site/hosting/restlet.nl?script=1&deploy=1",
        "tokenUrl":      "https://1234567.suitetalk.api.netsuite.com/services/rest/auth/oauth2/v1/token",
        "clientId":      "...",
        "certificateId": "...",
        "privateKey":    "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----"
      }'
```

Use `PUT` to update. On update, omit `privateKey` (or send it empty) to keep the stored key — useful
when only the account URL changes.

The response is `202 Accepted`, **not** confirmation that the credentials work. The configuration is
handed to the netsuite module, which stores it and then tries to authenticate. Poll:

```bash
curl "$API/api/v1/organisations/$ORG_ID/netsuite-configuration/status" -H "Authorization: Bearer $ADMIN_TOKEN"
```

| `syncState` | `netsuiteValid` | Meaning |
|---|---|---|
| *(no row)* | — | Nothing configured |
| `PENDING` | `null` | Stored locally, waiting for the netsuite module |
| `APPLIED` | `true` | Stored and authenticating — ready to ingest |
| `APPLIED` | `false` | Stored, but NetSuite rejected the credentials; see `validationMessage` |
| `FAILED` | — | The netsuite module could not store it; see `syncMessage` |

`APPLIED` alone is not sufficient — it means stored, not working. Wait for `netsuiteValid: true`.

The private key is never returned. The status endpoint reports only a SHA3 fingerprint, so you can
tell which key is installed without being able to read it.

## If an ingestion fails with NETSUITE_CONFIGURATION_NOT_FOUND

That organisation has no configuration stored in the netsuite module. Either it was never created,
or the configuration event never arrived (the status will still read `PENDING`). Re-submit the form
— including the private key, since the organisation module keeps no copy of it.

## How to set up IP Address Filtering
With the IP Address Filtering it is possible to restict access to the Netsuite account to certain IP Addresses. This can be done on a company level or on an employee level.
It is also possible to create a dummy user, which will then create the oAuth2 Login and this can be restricted to a specific IP Address.
1. Enable the IP Range filtering Feature
- Go to Setup > Company > Enable Features  --> Enable `IP ADDRESS RULES`
2. Adjust IP Addresses for Company
- Go to Setup > Company > Company Information --> Edit
- Add the IP Addresses you want to allow to access the Netsuite account under `ALLOWED IP ADDRESSES`
- Possible notation: 
  - Add a single IP Address (e.g. `123.45.67.80`)
  - Add a range of IP Addresses by using bitmask notation (e.g. `123.45.67.80/24`)
  - Add a range of IP Addresses by using a dash (e.g. `123.45.67.10-123.45.67.80`)
  - Add `NONE` to deny all access
  - Add `ALL` to allow all access
3. Adjust the allowed IP addresses by Employee
   - Go to Lists > Employees > Employees > The Employee you want to edit --> Edit
   - Under Access Tab adjust the `IP ADDRESS RESSTRICTIONS` to the desired IP Addresses
   - The user can inherit from company settings or have individual settings

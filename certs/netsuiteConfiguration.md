# Netsuite Configuration

## How to set up oAuth2
This document describes how to set up oAuth2 for the Netsuite ERP connector, which we are using at the Cardano Foundation.
It involves a few steps to set up the oAuth2 credentials in the Netsuite account and then use them in the connector.

1. Creating the certificate

To create a certificate the following command is needed
```bash
openssl req -new -x509 -newkey rsa:4096 -keyout private.pem -sigopt rsa_padding_mode:pss -sha256 -sigopt rsa_pss_saltlen:64 -out public.pem -nodes -days 365
```
This will create a certifate pair with RSA-PSS. The public part is needed to be uploaded to netsuite.
The private part must be kept secret and used in the connector. 
The path to this file must be passed to the application by leveraging the environment variable `LOB_NETSUITE_CLIENT_PRIVATE_KEY_FILE_PATH`.

2. Adding the certificate to Netsuite
- Create a new integration record in Netsuite 
  - Go to Setup > Integration > Manage Integrations > New 
  - When created you will see a client ID this is needed and needs to passed to the application by leveraging the environment variable`LOB_NETSUITE_CLIENT_CLIENT_ID`
  - **Attention**: You will see this clientID only once in Netsuite!
- Create Client Credential Setup (M2M)
  - Go to Setup > Integration > OAuth 2.0 Client Credentials Setup and click Create new
  - Choose an `Entity` and a `Role` and for `Application` choose the Integration you created in the previos step
  - Then Upload the public part of your certificate
  - After saving you will see a new row in the table. Copy the certificate ID and save it in the environment variable `LOB_NETSUITE_CLIENT_CERTIFICATE_ID`
3. Additional parameters needed:
- `LOB_NETSUITE_CLIENT_URL`: Base url of the Netsuite account (usally `https://<NETSUITE_ID>.restlets.api.netsuite.com/app/site/hosting/restlet.nl?<EXTRA PARAMS>`)
- `LOB_NETSUITE_CLIENT_TOKEN_URL`: Token url of the Netsuite account (usually `https://<NETSUITE_ID>.suitetalk.api.netsuite.com/services/rest/auth/oauth2/v1/token`)


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

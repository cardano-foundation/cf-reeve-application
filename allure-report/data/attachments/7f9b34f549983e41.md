# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: tests/e2e/chart-of-accounts.feature.spec.js >> Manager can add and maintain the organization chart of accounts, new active accounts >> Chart of account description is updated
- Location: .features-gen/tests/e2e/chart-of-accounts.feature.spec.js:20:7

# Error details

```
Error: Chart types should be requested successfully

expect(received).toEqual(expected) // deep equality

Expected: 200
Received: 401
```

# Test source

```ts
  1  | import {APIRequestContext, expect} from "@playwright/test";
  2  | import {faker} from "@faker-js/faker";
  3  | import {reeveService} from "../api/reeve-api/reeve.service";
  4  | import {HttpStatusCodes} from "../api/api-helpers/http-status-codes";
  5  | import {EventCodesDto} from "../api/dtos/eventCodesDto";
  6  | import {ChartOfAccountsDto, ChartTypeDto, UpdateChartOfAccountsDto} from "../api/dtos/chartOfAccountsDto";
  7  | 
  8  | export async function chartOfAccountsBuilder(request: APIRequestContext, authToken: string) {
  9  | 
  10 |     /**
  11 |      * Create the data for a new chart of account with only the required fields
  12 |      * customerCode Random 12 digits number, it doesn't exist in the system
  13 |      * name Random description with TEST prefix
  14 |      * type and subType organization chart types requested through API
  15 |      * eventRefCode debit reference code of an organization event code that has credit accounts,
  16 |      * so the new account can be used to import transactions
  17 |      */
  18 |     const createChartOfAccountData = async () => {
  19 |         const chartType = await getRandomChartType();
  20 |         const chartOfAccount: UpdateChartOfAccountsDto = {
  21 |             customerCode: faker.string.numeric({length: 12, allowLeadingZeros: false}),
  22 |             eventRefCode: await getUsableReferenceCode(),
  23 |             name: getRandomDescription(),
  24 |             type: chartType.id,
  25 |             subType: faker.helpers.arrayElement(chartType.subType).id,
  26 |             currency: "",
  27 |             counterParty: "",
  28 |             parentCustomerCode: "",
  29 |             active: true
  30 |         }
  31 |         return chartOfAccount
  32 |     }
  33 | 
  34 |     const createChartOfAccountDataWithNumber = async (customerCode: string) => {
  35 |         const chartOfAccount = await createChartOfAccountData();
  36 |         chartOfAccount.customerCode = customerCode;
  37 |         return chartOfAccount
  38 |     }
  39 | 
  40 |     const createChartOfAccountDataWithNewDescription = (chartOfAccount: UpdateChartOfAccountsDto) => {
  41 |         return {...chartOfAccount, name: getRandomDescription()}
  42 |     }
  43 | 
  44 |     const createDeactivatedChartOfAccountData = (chartOfAccount: UpdateChartOfAccountsDto) => {
  45 |         return {...chartOfAccount, active: false}
  46 |     }
  47 | 
  48 |     const getRandomDescription = () => "TEST " + faker.finance.accountName() + " " +
  49 |         Math.random().toString(36).substring(2, 2 + 8);
  50 | 
  51 |     const getRandomChartType = async () => {
  52 |         const chartTypesResponse = await (await reeveService(request)).getChartTypes(authToken);
  53 |         expect(chartTypesResponse.status(), "Chart types should be requested successfully")
> 54 |             .toEqual(HttpStatusCodes.success);
     |              ^ Error: Chart types should be requested successfully
  55 |         const chartTypes: ChartTypeDto[] = await chartTypesResponse.json();
  56 |         const chartTypesWithSubTypes = chartTypes.filter(chartType => chartType.subType?.length > 0);
  57 |         if (chartTypesWithSubTypes.length == 0) throw new Error("No chart type with subtypes found in the organization");
  58 |         return faker.helpers.arrayElement(chartTypesWithSubTypes)
  59 |     }
  60 | 
  61 |     /**
  62 |      * Get the debit reference code of an active event code
  63 |      * whose credit reference code has at least two active accounts,
  64 |      * so the transaction items can use different debit/credit pairs with the new account
  65 |      */
  66 |     const getUsableReferenceCode = async () => {
  67 |         const eventCodesResponse = await (await reeveService(request)).getEventCodes(authToken);
  68 |         expect(eventCodesResponse.status(), "Event codes should be requested successfully")
  69 |             .toEqual(HttpStatusCodes.success);
  70 |         const eventCodes: EventCodesDto[] = await eventCodesResponse.json();
  71 |         const chartOfAccountsResponse = await (await reeveService(request)).getChartOfAccounts(authToken);
  72 |         expect(chartOfAccountsResponse.status(), "Chart of accounts should be requested successfully")
  73 |             .toEqual(HttpStatusCodes.success);
  74 |         const chartOfAccounts: ChartOfAccountsDto[] = await chartOfAccountsResponse.json();
  75 |         const usableEventCodes = eventCodes.filter(eventCode => eventCode.active
  76 |             && eventCode.debitReferenceCode !== eventCode.creditReferenceCode
  77 |             && chartOfAccounts.filter(account => account.eventRefCode === eventCode.creditReferenceCode).length >= 2);
  78 |         if (usableEventCodes.length == 0) throw new Error("No event code with at least two credit accounts found in the organization");
  79 |         return faker.helpers.arrayElement(usableEventCodes).debitReferenceCode
  80 |     }
  81 | 
  82 |     return {
  83 |         createChartOfAccountData,
  84 |         createChartOfAccountDataWithNumber,
  85 |         createChartOfAccountDataWithNewDescription,
  86 |         createDeactivatedChartOfAccountData
  87 |     }
  88 | }
  89 | 
```
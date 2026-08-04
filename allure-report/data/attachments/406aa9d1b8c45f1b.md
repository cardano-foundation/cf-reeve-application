# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: tests/e2e/reprocess-transactions.feature.spec.js >> Users can reprocess transactions that are in pending status once the issue is solved, system checks >> Reprocess transaction in pending status by unknown account
- Location: .features-gen/tests/e2e/reprocess-transactions.feature.spec.js:27:7

# Error details

```
SyntaxError: Unexpected token '<', "<!doctype "... is not valid JSON
```

# Test source

```ts
  1   | import {APIRequestContext, expect} from "@playwright/test";
  2   | import {reeveApi} from "./reeve.api";
  3   | import {Batch, BatchData} from "../dtos/batchsDto";
  4   | import {BatchesStatusCodes} from "../api-helpers/batches-status-codes";
  5   | import {BatchResponse} from "../dtos/batchDto";
  6   | import {log} from "../../utils/logger";
  7   | import {RejectTransactionDto} from "../dtos/RejectTransactionDto";
  8   | import {UpdateCostCenterDto} from "../dtos/costCenterDto";
  9   | import {UpdateChartOfAccountsDto} from "../dtos/chartOfAccountsDto";
  10  | 
  11  | let managerUser = process.env.MANAGER_USER as string;
  12  | let managerPassword = process.env.MANAGER_PASSWORD as string;
  13  | let organizationId = process.env.ORGANIZATION_ID as string;
  14  | export async function reeveService(request: APIRequestContext) {
  15  |     const loginToReeve = async (userName: string, password: string) => {
  16  |         return await reeveApi(request).loginReeve(userName, password);
  17  |     };
  18  | 
  19  |     const loginManager = async () => {
  20  |         return await reeveApi(request).loginReeve(managerUser, managerPassword);
  21  |     }
  22  | 
  23  |     const getTransactionTypes = async (authToken: string) => {
  24  |         return await reeveApi(request).transactionTypes(authToken);
  25  |     }
  26  | 
  27  |     const getEventCodes = async (authToken: string) => {
  28  |         return await reeveApi(request).eventCodes(organizationId, authToken);
  29  |     }
  30  | 
  31  |     const getChartOfAccounts = async (authToken: string, customerCode?: string) => {
  32  |         return await reeveApi(request).chartOfAccounts(organizationId, authToken, customerCode);
  33  |     }
  34  | 
  35  |     const getCostCenters = async (authToken: string) => {
  36  |         return await reeveApi(request).getCostCenters(organizationId, authToken);
  37  |     }
  38  | 
  39  |     const updateCostCenter = async (authToken: string, costCenter: UpdateCostCenterDto) => {
  40  |         return await reeveApi(request).updateCostCenter(organizationId, authToken, costCenter);
  41  |     }
  42  | 
  43  |     const updateChartOfAccounts = async (authToken: string, chartOfAccount: UpdateChartOfAccountsDto) => {
  44  |         return await reeveApi(request).updateChartOfAccounts(organizationId, authToken, chartOfAccount);
  45  |     }
  46  | 
  47  |     const validateTransactionCsvFile = async (authToken: string, transactionFile: string) => {
  48  |         return await reeveApi(request).validateTransactionCsvFile(organizationId, authToken, transactionFile);
  49  |     }
  50  | 
  51  |     const importTransactionCsvFile = async (authToken: string, transactionFile: string) => {
  52  |         return await reeveApi(request).importTransactionCsvFile(organizationId,authToken, transactionFile)
  53  |     }
  54  | 
  55  |     const getBatchesByStatus = async (authToken: string, status: string) => {
  56  |         return await reeveApi(request).batchesByStatus(organizationId, authToken, status);
  57  |     }
  58  | 
  59  |     const getBatchById = async (authToken: string, batchId: string) => {
  60  |         return await reeveApi(request).batchById(authToken, batchId)
  61  |     }
  62  | 
  63  |     const findBatch = async (
  64  |         batchIds: string[],
  65  |         authToken: string,
  66  |         visitedBatchIds: Set<string>,
  67  |         matcher: (batch: BatchResponse) => boolean
  68  |     ) => {
  69  |         for (const batchId of batchIds) {
> 70  |             const batchDetailsResponse: BatchResponse = await (await getBatchById(authToken, batchId)).json();
      |                                                         ^ SyntaxError: Unexpected token '<', "<!doctype "... is not valid JSON
  71  |             visitedBatchIds.add(batchId);
  72  |             if (matcher(batchDetailsResponse)) {
  73  |                 return batchDetailsResponse;
  74  |             }
  75  |         }
  76  |         return null;
  77  |     }
  78  | 
  79  |     const pollForNewBatch = async (
  80  |         authToken: string,
  81  |         status: string,
  82  |         matcher: (batch: BatchResponse) => boolean
  83  |     ) => {
  84  |         let matchedBatch: BatchResponse;
  85  |         const visitedBatchIds = new Set<string>();
  86  |         await expect.poll(async () => {
  87  |             const batchesResponse = await getBatchesByStatus(authToken, status);
  88  |             const batchesAfterImport: BatchData = await batchesResponse.json();
  89  |             const allBatchIds = batchesAfterImport.batchs.map(batch => batch.id);
  90  |             const newBatchIds = allBatchIds.filter(id => !visitedBatchIds.has(id));
  91  |             matchedBatch = await findBatch(newBatchIds, authToken, visitedBatchIds, matcher);
  92  |             return matchedBatch;
  93  |         }, {
  94  |             message: "The new Batch was not created: ",
  95  |             intervals: [1_000, 2_000, 10_000],
  96  |             timeout: 280_000
  97  |         }).not.toBeNull();
  98  |         return matchedBatch;
  99  |     }
  100 | 
  101 |     const getNewBatch = async (authToken: string, status: string, txNumber: string) =>
  102 |         pollForNewBatch(authToken, status, batch => batch.transactions[0].internalTransactionNumber == txNumber);
  103 | 
  104 |     const getNewBatchByDocumentNumber = async (authToken: string, status: string, documentNumber: string) =>
  105 |         pollForNewBatch(authToken, status, batch => batch.transactions[0].items[0].documentNum == documentNumber);
  106 |     const reprocessBatch = async (authToken: string, batchId: string) => {
  107 |         return await reeveApi(request).reprocessBatch(authToken, batchId);
  108 |     }
  109 | 
  110 |     const approveTransaction = async (authToken: string, batchDetails: BatchResponse) => {
  111 |         return await reeveApi(request).approveTransaction(authToken, {
  112 |             organisationId: batchDetails.organisationId,
  113 |             transactionIds: batchDetails.transactions.map(tx => ({ id: tx.id }))
  114 |         });
  115 |     }
  116 | 
  117 |     const rejectTransaction = async (authToken: string, transactionToReject: RejectTransactionDto) => {
  118 |         return await reeveApi(request).rejectTransaction(authToken, transactionToReject)
  119 |     }
  120 |     const getTransactionById = async (authToken: string, transactionId: string) => {
  121 |         return await reeveApi(request).getTransactionById(authToken, transactionId)
  122 |     }
  123 |     return {
  124 |         loginToReeve,
  125 |         loginManager,
  126 |         getTransactionTypes,
  127 |         getEventCodes,
  128 |         getChartOfAccounts,
  129 |         getCostCenters,
  130 |         updateCostCenter,
  131 |         updateChartOfAccounts,
  132 |         validateTransactionCsvFile,
  133 |         importTransactionCsvFile,
  134 |         getBatchesByStatus,
  135 |         getNewBatch,
  136 |         getBatchById,
  137 |         getNewBatchByDocumentNumber,
  138 |         reprocessBatch,
  139 |         approveTransaction,
  140 |         rejectTransaction,
  141 |         getTransactionById
  142 |     };
  143 | 
  144 | }
```
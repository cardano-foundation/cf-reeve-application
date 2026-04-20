import {expect} from '@playwright/test';
import {Given, When, Then} from "../../utils/playwright-bdd-fixtures";
import {reeveService} from "../../api/reeve-api/reeve.service";
import {HttpStatusCodes} from "../../api/api-helpers/http-status-codes";
import {transactionsBuilder} from "../../helpers/transactionsBuilder";
import {BatchesStatusCodes} from "../../api/api-helpers/batches-status-codes";
import {BatchResponse} from "../../api/dtos/batchDto";
import {transactionValidator} from "../../validators/transactionValidator";
import {TransactionPendingInvalidStatus} from "../../helpers/transaction-pending-invalid-status";
import {deleteFile} from "../../utils/csvFileGenerator";

Given(/^Manager user wants to import a transaction with a CSV file$/, async ({request, ctx}) => {
    const loginResponse = await (await reeveService(request)).loginManager()
    expect(loginResponse.status()).toEqual(HttpStatusCodes.success)
    ctx.authToken = (await loginResponse.json()).token_type + " " + (await loginResponse.json()).access_token;
});
Given(/^the manager creates the CSV file with all the required fields$/, async ({request,ctx}) => {
    ctx.transactionCSVFile = await (await transactionsBuilder(request, ctx.authToken))
        .createReadyToApproveTransaction(ctx.transactionDataToImport);
});
Given(/^system get the validation request$/, async ({request,ctx}) => {
    const validateResponse = await (await reeveService(request)).validateTransactionCsvFile(ctx.authToken,
        ctx.transactionCSVFile);
    expect(validateResponse.status()).toEqual(HttpStatusCodes.success);
});
When(/^system get import request$/, async ({request, ctx}) => {
    const importTxCsvResponse = await (await reeveService(request)).importTransactionCsvFile(ctx.authToken,
        ctx.transactionCSVFile);
    expect(importTxCsvResponse.status()).toEqual(HttpStatusCodes.RequestAccepted);
    await deleteFile(ctx.transactionCSVFile)
});
Then(/^the transaction data should be imported with ready to approve status$/, async ({request, ctx}) => {
    const newBatchAfterImport = await (await reeveService(request)).getNewBatch(ctx.authToken,
        BatchesStatusCodes.APPROVE, ctx.transactionDataToImport[0].TxNumber);
    const batchDetailsResponse = await (await reeveService(request)).getBatchById(ctx.authToken,
        newBatchAfterImport.id);
    expect(batchDetailsResponse.status()).toEqual(HttpStatusCodes.success);
    let importedBatchDetails: BatchResponse =  await batchDetailsResponse.json()
    await (await transactionValidator()).validateImportedTxWithStatus(ctx.transactionDataToImport, importedBatchDetails,
        BatchesStatusCodes.APPROVE);
});
Given(/^the cost center data in the CSV file doesn't exist in the system$/, async ({request, ctx}) => {
    ctx.transactionCSVFile = await (await transactionsBuilder(request, ctx.authToken))
        .createCSVTransactionPending(ctx.transactionDataToImport, TransactionPendingInvalidStatus.COST_CENTER_DATA_NOT_FOUND);
});
Then(/^the system should create the transaction with pending status by "([^"]*)"$/, async ({request, ctx}, reason) => {
    const newBatchAfterImport = await (await reeveService(request)).getNewBatch(ctx.authToken,
        BatchesStatusCodes.PENDING, ctx.transactionDataToImport[0].TxNumber);
    await (await transactionValidator()).validateImportedTxWithStatus(ctx.transactionDataToImport, newBatchAfterImport,
        BatchesStatusCodes.PENDING);
    await (await transactionValidator()).validatePendingCondition(newBatchAfterImport,
        reason)
});
Given(/^the vat code data in the CSV file doesn't exist in the system$/, async ({request, ctx}) => {
    ctx.transactionCSVFile = await (await transactionsBuilder(request, ctx.authToken))
        .createCSVTransactionPending(ctx.transactionDataToImport, TransactionPendingInvalidStatus.VAT_DATA_NOT_FOUND);
});
Given(/^the chart of account code data in the CSV file doesn't exist in the system$/, async ({request, ctx}) => {
    ctx.transactionCSVFile = await (await transactionsBuilder(request, ctx.authToken))
        .createCSVTransactionPending(ctx.transactionDataToImport, TransactionPendingInvalidStatus.CHART_OF_ACCOUNT_NOT_FOUND);
});
Given(/^the transaction amounts are not balanced$/, async ({request, ctx}) => {
    ctx.transactionCSVFile = await (await transactionsBuilder(request, ctx.authToken))
        .createCSVTransactionInvalid(ctx.transactionDataToImport, TransactionPendingInvalidStatus.UNBALANCED_TRANSACTION);
});
Then(/^the system should create the transaction with invalid status by "([^"]*)"$/, async ({request, ctx}, reason) => {
    const newBatchAfterImport = await (await reeveService(request)).getNewBatchByDocumentNumber(ctx.authToken,
        BatchesStatusCodes.INVALID, ctx.transactionDataToImport[0].DocumentName);
    await (await transactionValidator()).validateImportedTxWithStatus(ctx.transactionDataToImport, newBatchAfterImport,
        BatchesStatusCodes.INVALID);
    await (await transactionValidator()).validateInvalidCondition(newBatchAfterImport,
        reason)
});
Given(/^the transaction number is missing in the CSV file$/, async ({request, ctx}) => {
    ctx.transactionCSVFile = await (await transactionsBuilder(request, ctx.authToken))
        .createCSVTransactionInvalid(ctx.transactionDataToImport, TransactionPendingInvalidStatus.TX_INTERNAL_NUMBER_MUST_BE_PRESENT);

});
Given(/^the transaction debit code is missing in the CSV file$/, async ({request, ctx}) => {
    ctx.transactionCSVFile = await (await transactionsBuilder(request, ctx.authToken))
        .createCSVTransactionInvalid(ctx.transactionDataToImport, TransactionPendingInvalidStatus.ACCOUNT_CODE_DEBIT_IS_EMPTY);
});
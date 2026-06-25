import {expect} from '@playwright/test';
import {Given, When, Then} from "../../utils/playwright-bdd-fixtures";
import {reeveService} from "../../api/reeve-api/reeve.service";
import {HttpStatusCodes} from "../../api/api-helpers/http-status-codes";
import {BatchesStatusCodes} from "../../api/api-helpers/batches-status-codes";
import {commonTestActions} from "../../helpers/common-test-actions";
import {transactionsBuilder} from "../../helpers/transactionsBuilder";
import {CostCenterDto} from "../../api/dtos/costCenterDto";
import {deleteFile} from "../../utils/csvFileGenerator";
import {transactionValidator} from "../../validators/transactionValidator";

Given(/^there is an imported transaction in pending status due to cost center mapping$/, async ({request, ctx}) => {
    ctx.auth.authToken = await (await commonTestActions(request)).loginUser();

    const costCentersResponse = await (await reeveService(request)).getCostCenters(ctx.auth.authToken);
    expect(costCentersResponse.status()).toEqual(HttpStatusCodes.success);
    const costCenters: CostCenterDto[] = await costCentersResponse.json();

    const activeCostCenters = costCenters.filter(costCenter => costCenter.active);
    const randomIndex = Math.floor(Math.random() * activeCostCenters.length);
    const selectedCostCenter = activeCostCenters[randomIndex];
    ctx.costCenter.costCenterCode = selectedCostCenter.customerCode;
    ctx.costCenter.costCenterName = selectedCostCenter.name;

    const deactivateResponse = await (await reeveService(request)).updateCostCenter(ctx.auth.authToken, {
        customerCode: selectedCostCenter.customerCode,
        name: selectedCostCenter.name,
        active: false
    });
    expect(deactivateResponse.status()).toEqual(HttpStatusCodes.success);

    ctx.transaction.transactionCSVFile = await (await transactionsBuilder(request, ctx.auth.authToken))
        .createCSVTransactionPendingByCostCenter(ctx.transaction.transactionDataToImport, ctx.costCenter.costCenterCode);

    const validateResponse = await (await reeveService(request)).validateTransactionCsvFile(ctx.auth.authToken,
        ctx.transaction.transactionCSVFile);
    expect(validateResponse.status()).toEqual(HttpStatusCodes.success);

    const importTxCsvResponse = await (await reeveService(request)).importTransactionCsvFile(ctx.auth.authToken,
        ctx.transaction.transactionCSVFile);
    expect(importTxCsvResponse.status()).toEqual(HttpStatusCodes.RequestAccepted);
    await deleteFile(ctx.transaction.transactionCSVFile);

    ctx.transaction.batchDetails = await (await reeveService(request)).getNewBatch(
        ctx.auth.authToken, BatchesStatusCodes.PENDING, ctx.transaction.transactionDataToImport[0].TxNumber
    );
});

Given(/^the issue with the cost center is adjusted in the system$/, async ({request, ctx}) => {
    const reactivateResponse = await (await reeveService(request)).updateCostCenter(ctx.auth.authToken, {
        customerCode: ctx.costCenter.costCenterCode,
        name: ctx.costCenter.costCenterName,
        active: true
    });
    expect(reactivateResponse.status()).toEqual(HttpStatusCodes.success);
});

When(/^the system process the reprocess request$/, async ({request, ctx}) => {
    const reprocessResponse = await (await reeveService(request)).reprocessBatch(ctx.auth.authToken, ctx.transaction.batchDetails.id);
    expect(reprocessResponse.status()).toEqual(HttpStatusCodes.success);
});

Then(/^the transaction should change the status to ready to approve$/, async ({request, ctx}) => {
    const reprocessedBatch = await (await reeveService(request)).getNewBatch(
        ctx.auth.authToken, BatchesStatusCodes.APPROVE, ctx.transaction.transactionDataToImport[0].TxNumber
    );
    await (await transactionValidator()).validateImportedTxWithStatus(
        ctx.transaction.transactionDataToImport, reprocessedBatch, BatchesStatusCodes.APPROVE
    );
});
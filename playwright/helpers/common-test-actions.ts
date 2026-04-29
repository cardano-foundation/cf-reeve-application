import {APIRequestContext, expect} from "@playwright/test";
import {reeveService} from "../api/reeve-api/reeve.service";
import {HttpStatusCodes} from "../api/api-helpers/http-status-codes";
import {transactionsBuilder} from "./transactionsBuilder";
import {TransactionItemCsvDto} from "../api/dtos/transactionItemCsvDto";
import {deleteFile} from "../utils/csvFileGenerator";
import {BatchesStatusCodes} from "../api/api-helpers/batches-status-codes";
import {transactionValidator} from "../validators/transactionValidator";

export async function commonTestActions(request: APIRequestContext) {
    const loginUser = async () => {
        const loginResponse = await (await reeveService(request)).loginManager()
        expect(loginResponse.status()).toEqual(HttpStatusCodes.success)
        return (await loginResponse.json()).token_type + " " + (await loginResponse.json()).access_token;

    }
    const importReadyToApproveTx = async (authToken: string, transactionDataToImport: TransactionItemCsvDto[]) => {
        const transactionCSVFile = await (await transactionsBuilder(request, authToken))
            .createReadyToApproveTransaction(transactionDataToImport);
        const validateResponse = await (await reeveService(request)).validateTransactionCsvFile(authToken,
            transactionCSVFile);
        expect(validateResponse.status()).toEqual(HttpStatusCodes.success);
        const importTxCsvResponse = await (await reeveService(request)).importTransactionCsvFile(authToken,
            transactionCSVFile);
        expect(importTxCsvResponse.status()).toEqual(HttpStatusCodes.RequestAccepted);
        await deleteFile(transactionCSVFile)
        const newBatchAfterImport = await (await reeveService(request)).getNewBatch(authToken,
            BatchesStatusCodes.APPROVE, transactionDataToImport[0].TxNumber);
        await (await transactionValidator()).validateImportedTxWithStatus(transactionDataToImport, newBatchAfterImport,
            BatchesStatusCodes.APPROVE);
        return newBatchAfterImport
    }

    return {
        loginUser,
        importReadyToApproveTx
    }
}
import {APIRequestContext, expect} from "@playwright/test";
import {reeveApi} from "./reeve.api";
import {Batch, BatchData} from "../dtos/batchsDto";
import {BatchesStatusCodes} from "../api-helpers/batches-status-codes";
import {BatchResponse} from "../dtos/batchDto";
import {log} from "../../utils/logger";
import {RejectTransactionDto} from "../dtos/RejectTransactionDto";
import {UpdateCostCenterDto} from "../dtos/costCenterDto";

let managerUser = process.env.MANAGER_USER as string;
let managerPassword = process.env.MANAGER_PASSWORD as string;
let organizationId = process.env.ORGANIZATION_ID as string;
export async function reeveService(request: APIRequestContext) {
    const loginToReeve = async (userName: string, password: string) => {
        return await reeveApi(request).loginReeve(userName, password);
    };

    const loginManager = async () => {
        return await reeveApi(request).loginReeve(managerUser, managerPassword);
    }

    const getTransactionTypes = async (authToken: string) => {
        return await reeveApi(request).transactionTypes(authToken);
    }

    const getEventCodes = async (authToken: string) => {
        return await reeveApi(request).eventCodes(organizationId, authToken);
    }

    const getChartOfAccounts = async (authToken: string) => {
        return await reeveApi(request).chartOfAccounts(organizationId, authToken);
    }

    const getCostCenters = async (authToken: string) => {
        return await reeveApi(request).getCostCenters(organizationId, authToken);
    }

    const updateCostCenter = async (authToken: string, costCenter: UpdateCostCenterDto) => {
        return await reeveApi(request).updateCostCenter(organizationId, authToken, costCenter);
    }

    const validateTransactionCsvFile = async (authToken: string, transactionFile: string) => {
        return await reeveApi(request).validateTransactionCsvFile(organizationId, authToken, transactionFile);
    }

    const importTransactionCsvFile = async (authToken: string, transactionFile: string) => {
        return await reeveApi(request).importTransactionCsvFile(organizationId,authToken, transactionFile)
    }

    const getBatchesByStatus = async (authToken: string, status: string) => {
        return await reeveApi(request).batchesByStatus(organizationId, authToken, status);
    }

    const getBatchById = async (authToken: string, batchId: string) => {
        return await reeveApi(request).batchById(authToken, batchId)
    }

    const findBatch = async (
        batchIds: string[],
        authToken: string,
        visitedBatchIds: Set<string>,
        matcher: (batch: BatchResponse) => boolean
    ) => {
        for (const batchId of batchIds) {
            const batchDetailsResponse: BatchResponse = await (await getBatchById(authToken, batchId)).json();
            visitedBatchIds.add(batchId);
            if (matcher(batchDetailsResponse)) {
                return batchDetailsResponse;
            }
        }
        return null;
    }

    const pollForNewBatch = async (
        authToken: string,
        status: string,
        matcher: (batch: BatchResponse) => boolean
    ) => {
        let matchedBatch: BatchResponse;
        const visitedBatchIds = new Set<string>();
        await expect.poll(async () => {
            const batchesResponse = await getBatchesByStatus(authToken, status);
            const batchesAfterImport: BatchData = await batchesResponse.json();
            const allBatchIds = batchesAfterImport.batchs.map(batch => batch.id);
            const newBatchIds = allBatchIds.filter(id => !visitedBatchIds.has(id));
            matchedBatch = await findBatch(newBatchIds, authToken, visitedBatchIds, matcher);
            return matchedBatch;
        }, {
            message: "The new Batch was not created: ",
            intervals: [1_000, 2_000, 10_000],
            timeout: 280_000
        }).not.toBeNull();
        return matchedBatch;
    }

    const getNewBatch = async (authToken: string, status: string, txNumber: string) =>
        pollForNewBatch(authToken, status, batch => batch.transactions[0].internalTransactionNumber == txNumber);

    const getNewBatchByDocumentNumber = async (authToken: string, status: string, documentNumber: string) =>
        pollForNewBatch(authToken, status, batch => batch.transactions[0].items[0].documentNum == documentNumber);
    const reprocessBatch = async (authToken: string, batchId: string) => {
        return await reeveApi(request).reprocessBatch(authToken, batchId);
    }

    const rejectTransaction = async (authToken: string, transactionToReject: RejectTransactionDto) => {
        return await reeveApi(request).rejectTransaction(authToken, transactionToReject)
    }
    const getTransactionById = async (authToken: string, transactionId: string) => {
        return await reeveApi(request).getTransactionById(authToken, transactionId)
    }
    return {
        loginToReeve,
        loginManager,
        getTransactionTypes,
        getEventCodes,
        getChartOfAccounts,
        getCostCenters,
        updateCostCenter,
        validateTransactionCsvFile,
        importTransactionCsvFile,
        getBatchesByStatus,
        getNewBatch,
        getBatchById,
        getNewBatchByDocumentNumber,
        reprocessBatch,
        rejectTransaction,
        getTransactionById
    };

}
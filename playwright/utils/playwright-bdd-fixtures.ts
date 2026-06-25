import {test as base, createBdd} from 'playwright-bdd';
import {TransactionItemCsvDto} from "../api/dtos/transactionItemCsvDto";
import {BatchResponse} from "../api/dtos/batchDto";
import {RejectTransactionDto, RejectTransactionResponseDto} from "../api/dtos/RejectTransactionDto";

interface AuthContext {
    authToken: string;
}

interface TransactionContext {
    batchDetails?: BatchResponse;

    transactionCSVFile?: string;
    transactionDataToImport: TransactionItemCsvDto[];

    rejectTransactionData?: RejectTransactionDto
    rejectTransactionResponse?: RejectTransactionResponseDto

}

interface CostCenterContext {
    costCenterCode?: string;
    costCenterName?: string;
}

interface ScenarioContext {
    auth: AuthContext
    transaction: TransactionContext
    costCenter: CostCenterContext
}

export const test = base.extend<{ ctx: ScenarioContext }>({
    ctx: async ({}, use) => {
        await use({
            auth: {
                authToken: '',
            },
            transaction: {
                transactionDataToImport: []
            },
            costCenter: {}
        });
    }
});

export const { Given, When, Then } = createBdd(test);
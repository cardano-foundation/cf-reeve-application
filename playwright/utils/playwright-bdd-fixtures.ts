import {test as base, createBdd} from 'playwright-bdd';
import {TransactionItemCsvDto} from "../api/dtos/transactionItemCsvDto";

interface ScenarioContext {
    authToken: string;
    transactionCSVFile: string;
    transactionDataToImport: TransactionItemCsvDto[];
}

export const test = base.extend<{ ctx: ScenarioContext }>({
    ctx: async ({}, use) => {
        await use({
            authToken: '',
            transactionCSVFile: '',
            transactionDataToImport: []
        });
    }
});

export const { Given, When, Then } = createBdd(test);
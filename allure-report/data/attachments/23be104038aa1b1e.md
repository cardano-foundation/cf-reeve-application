# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: tests/e2e/Import-transactions-CSV.feature.spec.js >> Users can import transactions into Reeve with a CSV file, system validates the structure file >> Import transaction in pending status by unknown VAT code
- Location: .features-gen/tests/e2e/Import-transactions-CSV.feature.spec.js:22:7

# Error details

```
SyntaxError: Unexpected end of JSON input
```

# Test source

```ts
  58  |     /**
  59  |      * Create a transaction with just two transactions items,
  60  |      * txNumber Random short hash
  61  |      * documentName Random short hash
  62  |      * txType organization transaction type requested through API
  63  |      * debitTxItem accounts are requested through API in base of organization event codes
  64  |      * creditTxItem accounts are requested through API in base of organization event codes
  65  |      */
  66  |     const createValidTransactionData = async (transactionDataToImport: TransactionItemCsvDto[]) => {
  67  |         const transactionCommonData = await getTransactionCommonData()
  68  |         const amountForTxItem = (Math.floor(Math.random() * 100000) + 1).toString();
  69  |         const eventCodes = await getEventCodes();
  70  |         const debitAndCreditAccounts = await getDebitAndCreditAccounts(eventCodes);
  71  |         const debitTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  72  |             true, debitAndCreditAccounts);
  73  |         const creditTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  74  |             false, debitAndCreditAccounts);
  75  |         transactionDataToImport.push(debitTxItem);
  76  |         transactionDataToImport.push(creditTxItem);
  77  |         const rows: string[][] = [];
  78  |         rows.push(Object.values(debitTxItem));
  79  |         rows.push(Object.values(creditTxItem))
  80  |         return rows
  81  |     }
  82  |     const createPendingTransactionData = async (transactionDataToImport: TransactionItemCsvDto[], pendingReason: string) => {
  83  |         const transactionCommonData = await getTransactionCommonData()
  84  |         const amountForTxItem = (Math.floor(Math.random() * 100000) + 1).toString();
  85  |         const eventCodes = await getEventCodes();
  86  |         const debitAndCreditAccounts = await getDebitAndCreditAccounts(eventCodes);
  87  |         const debitTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  88  |             true, debitAndCreditAccounts)
  89  |         await setPendingReason(debitTxItem, pendingReason);
  90  |         const creditTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  91  |             false, debitAndCreditAccounts);
  92  |         transactionDataToImport.push(debitTxItem);
  93  |         transactionDataToImport.push(creditTxItem);
  94  |         const rows: string[][] = [];
  95  |         rows.push(Object.values(debitTxItem));
  96  |         rows.push(Object.values(creditTxItem))
  97  |         return rows
  98  |     }
  99  |     const createInvalidTransactionData = async (transactionDataToImport: TransactionItemCsvDto[], invalidReason: string) => {
  100 |         const transactionCommonData = await getTransactionCommonData()
  101 |         const amountForTxItem = (Math.floor(Math.random() * 100000) + 1).toString();
  102 |         const eventCodes = await getEventCodes();
  103 |         const debitAndCreditAccounts = await getDebitAndCreditAccounts(eventCodes);
  104 |         const debitTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  105 |             true, debitAndCreditAccounts)
  106 |         const creditTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  107 |             false, debitAndCreditAccounts);
  108 |         await setInvalidReason(debitTxItem, creditTxItem,invalidReason);
  109 |         transactionDataToImport.push(debitTxItem);
  110 |         transactionDataToImport.push(creditTxItem);
  111 |         const rows: string[][] = [];
  112 |         rows.push(Object.values(debitTxItem));
  113 |         rows.push(Object.values(creditTxItem))
  114 |         return rows
  115 |     }
  116 |     const getTransactionCommonData = async () => {
  117 |         const txNumber = "TEST-" + Math.random().toString(36).substring(2, 2 + 8);
  118 |         const txDate = getDateInThePast(2, true);
  119 |         const txType = await getTransactionType();
  120 |         const documentName = "TEST-" + Math.random().toString(36).substring(2, 2 + 8);
  121 |         const transactionItemCommonData: TransactionItemCsvDto = {
  122 |             TxNumber: txNumber,
  123 |             TxDate: txDate,
  124 |             TxType: txType,
  125 |             DocumentName: documentName
  126 |         }
  127 |         return transactionItemCommonData
  128 |     }
  129 |     const setPendingReason = async (transactionItem: TransactionItemCsvDto, pendingReason: string) => {
  130 |         if(pendingReason == TransactionPendingInvalidStatus.COST_CENTER_DATA_NOT_FOUND){
  131 |             transactionItem.TxCostCenter = Math.random().toString(36).substring(2, 2 + 8);
  132 |         }
  133 |         if(pendingReason == TransactionPendingInvalidStatus.VAT_DATA_NOT_FOUND){
  134 |             transactionItem.VatCode = Math.random().toString(36).substring(2, 2 + 8);
  135 |         }
  136 |         if(pendingReason == TransactionPendingInvalidStatus.CHART_OF_ACCOUNT_NOT_FOUND){
  137 |             transactionItem.DebitCode = Math.random().toString(36).substring(2, 2 + 8);
  138 |         }
  139 |     }
  140 |     const setInvalidReason = async (debitTransactionItem: TransactionItemCsvDto, creditTransactionItem: TransactionItemCsvDto,
  141 |                                     invalidReason: string) => {
  142 |         if(invalidReason == TransactionPendingInvalidStatus.UNBALANCED_TRANSACTION){
  143 |             debitTransactionItem.AmountLcyDebit = (Number(debitTransactionItem.AmountLcyDebit) + 1000).toString();
  144 |             debitTransactionItem.AmountFcyDebit = (Number(debitTransactionItem.AmountFcyDebit) + 1000).toString();
  145 |         }
  146 |         if(invalidReason == TransactionPendingInvalidStatus.TX_INTERNAL_NUMBER_MUST_BE_PRESENT){
  147 |             debitTransactionItem.TxNumber = "";
  148 |             creditTransactionItem.TxNumber= "";
  149 |         }
  150 |         if(invalidReason == TransactionPendingInvalidStatus.ACCOUNT_CODE_DEBIT_IS_EMPTY){
  151 |             debitTransactionItem.DebitCode = "";
  152 |         }
  153 |     }
  154 |     const getTransactionType = async () => {
  155 |         const transactionTypeResponse = await (await reeveService(request))
  156 |             .getTransactionTypes(authToken);
  157 |         expect(transactionTypeResponse.status()).toEqual(HttpStatusCodes.success);
> 158 |         const transactionTypes: TransactionTypeDto[] = await (transactionTypeResponse.json());
      |                                                        ^ SyntaxError: Unexpected end of JSON input
  159 |         const randomTxType = Math.floor(Math.random() * (transactionTypes.length - 1));
  160 |         return (transactionTypes[randomTxType].id)
  161 |     }
  162 | 
  163 |     const getEventCodes = async () => {
  164 |         const eventCodesResponse = await (await reeveService(request)).getEventCodes(authToken);
  165 |         expect(eventCodesResponse.status()).toEqual(HttpStatusCodes.success);
  166 |         const eventCodes: EventCodesDto[] = await (eventCodesResponse.json());
  167 |         const referenceCodes: ReferenceCodePair[] = eventCodes.map(eventCode => ({
  168 |             debitReferenceCode: eventCode.debitReferenceCode,
  169 |             creditReferenceCode: eventCode.creditReferenceCode
  170 |         }));
  171 |         return referenceCodes;
  172 |     }
  173 | 
  174 |     /**
  175 |      * Get two lists of accounts that has an event code
  176 |      * for the combination of debit and credit accounts
  177 |      * @param eventCodes array of organization's event codes
  178 |      *
  179 |      */
  180 |     const getDebitAndCreditAccounts = async (eventCodes: ReferenceCodePair[]) => {
  181 |         const chartOfAccounts: AccountRefCodePair[] = await getChartOfAccounts();
  182 |         let debitAccounts: AccountCodeAndNamePair[] | null;
  183 |         let creditAccounts: AccountCodeAndNamePair[] | null;
  184 |         for (const eventCode of eventCodes) {
  185 |             if (eventCode.debitReferenceCode !== eventCode.creditReferenceCode) {
  186 |                 debitAccounts = chartOfAccounts
  187 |                     .filter(account => account.referenceCode === eventCode.debitReferenceCode)
  188 |                     .map(account => ({ accountCode: account.accountCode, accountName: account.accountName }));
  189 |                 if (debitAccounts.length >= 1) {
  190 |                     creditAccounts = chartOfAccounts
  191 |                         .filter(account => account.referenceCode === eventCode.creditReferenceCode
  192 |                             && account.accountCode !== debitAccounts[0].accountCode)
  193 |                         .map(account => ({ accountCode: account.accountCode, accountName: account.accountName }));
  194 |                 }
  195 |                 if (creditAccounts != null) break;
  196 |             }
  197 |         }
  198 |         if (!creditAccounts) throw new Error("No valid debit/credit account pair found in event codes");
  199 |         const debitAndCreditAccounts: DebitAndCreditAccounts = {
  200 |             debitAccounts: debitAccounts,
  201 |             creditAccounts: creditAccounts
  202 |         }
  203 |         return debitAndCreditAccounts
  204 |     }
  205 | 
  206 |     const getChartOfAccounts = async () => {
  207 |         const chartOfAccountsResponse = await (await reeveService(request)).getChartOfAccounts(authToken);
  208 |         expect(chartOfAccountsResponse.status()).toEqual(HttpStatusCodes.success);
  209 |         const chartOfAccounts: AccountRefCodePair[] = (await (chartOfAccountsResponse).json())
  210 |             .map(chartOfAccount => ({
  211 |                 accountCode: chartOfAccount.customerCode,
  212 |                 referenceCode: chartOfAccount.eventRefCode,
  213 |                 accountName: chartOfAccount.name
  214 |             }))
  215 |         return chartOfAccounts
  216 |     }
  217 | 
  218 |     const createTransactionItem = async (transactionItemCommonData: TransactionItemCsvDto, amount: string,
  219 |                                          isDebit: boolean, debitAndCreditAccounts: DebitAndCreditAccounts) => {
  220 |         let randomIndexDebit = Math.floor(Math.random() * debitAndCreditAccounts.debitAccounts.length)
  221 |         let randomIndexCredit = Math.floor(Math.random() * debitAndCreditAccounts.creditAccounts.length)
  222 |         const transactionItem: TransactionItemCsvDto = {
  223 |             TxNumber: transactionItemCommonData.TxNumber,
  224 |             TxDate: transactionItemCommonData.TxDate,
  225 |             TxType: transactionItemCommonData.TxType,
  226 |             FxRate: "1",
  227 |             AmountLcyDebit: "",
  228 |             AmountLcyCredit: "",
  229 |             AmountFcyDebit: "",
  230 |             AmountFcyCredit: "",
  231 |             DebitCode: "",
  232 |             DebitName: "",
  233 |             CreditCode: "",
  234 |             CreditName: "",
  235 |             ProjectCode: "",
  236 |             DocumentName: transactionItemCommonData.DocumentName,
  237 |             TxCurrency: "CHF",
  238 |             VatRate: "",
  239 |             VatCode: "",
  240 |             TxCostCenter: "",
  241 |             CounterParty: "",
  242 |             CounterpartyName: "",
  243 |         }
  244 |         if (isDebit) {
  245 |             transactionItem.AmountLcyDebit = amount;
  246 |             transactionItem.AmountFcyDebit = amount;
  247 |         } else {
  248 |             transactionItem.AmountLcyCredit = amount;
  249 |             transactionItem.AmountFcyCredit = amount;
  250 |         }
  251 |         transactionItem.DebitCode = debitAndCreditAccounts.debitAccounts[randomIndexDebit].accountCode;
  252 |         transactionItem.DebitName = debitAndCreditAccounts.debitAccounts[randomIndexDebit].accountName;
  253 |         transactionItem.CreditCode = debitAndCreditAccounts.creditAccounts[randomIndexCredit].accountCode;
  254 |         transactionItem.CreditName = debitAndCreditAccounts.creditAccounts[randomIndexCredit].accountName;
  255 |         return transactionItem
  256 |     }
  257 | 
  258 |     return {
```
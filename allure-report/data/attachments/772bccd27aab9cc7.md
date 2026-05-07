# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: tests/e2e/Import-transactions-CSV.feature.spec.js >> Users can import transactions into Reeve with a CSV file, system validates the structure file >> Import transaction in pending status by unknown VAT code
- Location: .features-gen/tests/e2e/Import-transactions-CSV.feature.spec.js:22:7

# Error details

```
Error: expect(received).toEqual(expected) // deep equality

Expected: 200
Received: 500
```

# Test source

```ts
  48  | 
  49  |     /**
  50  |      * Create a transaction with just two transactions items,
  51  |      * txNumber Random short hash
  52  |      * documentName Random short hash
  53  |      * txType organization transaction type requested through API
  54  |      * debitTxItem accounts are requested through API in base of organization event codes
  55  |      * creditTxItem accounts are requested through API in base of organization event codes
  56  |      */
  57  |     const createValidTransactionData = async (transactionDataToImport: TransactionItemCsvDto[]) => {
  58  |         const transactionCommonData = await getTransactionCommonData()
  59  |         const amountForTxItem = (Math.floor(Math.random() * 100000) + 1).toString();
  60  |         const eventCodes = await getEventCodes();
  61  |         const debitAndCreditAccounts = await getDebitAndCreditAccounts(eventCodes);
  62  |         const debitTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  63  |             true, debitAndCreditAccounts);
  64  |         const creditTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  65  |             false, debitAndCreditAccounts);
  66  |         transactionDataToImport.push(debitTxItem);
  67  |         transactionDataToImport.push(creditTxItem);
  68  |         const rows: string[][] = [];
  69  |         rows.push(Object.values(debitTxItem));
  70  |         rows.push(Object.values(creditTxItem))
  71  |         return rows
  72  |     }
  73  |     const createPendingTransactionData = async (transactionDataToImport: TransactionItemCsvDto[], pendingReason: string) => {
  74  |         const transactionCommonData = await getTransactionCommonData()
  75  |         const amountForTxItem = (Math.floor(Math.random() * 100000) + 1).toString();
  76  |         const eventCodes = await getEventCodes();
  77  |         const debitAndCreditAccounts = await getDebitAndCreditAccounts(eventCodes);
  78  |         const debitTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  79  |             true, debitAndCreditAccounts)
  80  |         await setPendingReason(debitTxItem, pendingReason);
  81  |         const creditTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  82  |             false, debitAndCreditAccounts);
  83  |         transactionDataToImport.push(debitTxItem);
  84  |         transactionDataToImport.push(creditTxItem);
  85  |         const rows: string[][] = [];
  86  |         rows.push(Object.values(debitTxItem));
  87  |         rows.push(Object.values(creditTxItem))
  88  |         return rows
  89  |     }
  90  |     const createInvalidTransactionData = async (transactionDataToImport: TransactionItemCsvDto[], invalidReason: string) => {
  91  |         const transactionCommonData = await getTransactionCommonData()
  92  |         const amountForTxItem = (Math.floor(Math.random() * 100000) + 1).toString();
  93  |         const eventCodes = await getEventCodes();
  94  |         const debitAndCreditAccounts = await getDebitAndCreditAccounts(eventCodes);
  95  |         const debitTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  96  |             true, debitAndCreditAccounts)
  97  |         const creditTxItem = await createTransactionItem(transactionCommonData, amountForTxItem,
  98  |             false, debitAndCreditAccounts);
  99  |         await setInvalidReason(debitTxItem, creditTxItem,invalidReason);
  100 |         transactionDataToImport.push(debitTxItem);
  101 |         transactionDataToImport.push(creditTxItem);
  102 |         const rows: string[][] = [];
  103 |         rows.push(Object.values(debitTxItem));
  104 |         rows.push(Object.values(creditTxItem))
  105 |         return rows
  106 |     }
  107 |     const getTransactionCommonData = async () => {
  108 |         const txNumber = "TEST-" + Math.random().toString(36).substring(2, 2 + 8);
  109 |         const txDate = getDateInThePast(2, true);
  110 |         const txType = await getTransactionType();
  111 |         const documentName = "TEST-" + Math.random().toString(36).substring(2, 2 + 8);
  112 |         const transactionItemCommonData: TransactionItemCsvDto = {
  113 |             TxNumber: txNumber,
  114 |             TxDate: txDate,
  115 |             TxType: txType,
  116 |             DocumentName: documentName
  117 |         }
  118 |         return transactionItemCommonData
  119 |     }
  120 |     const setPendingReason = async (transactionItem: TransactionItemCsvDto, pendingReason: string) => {
  121 |         if(pendingReason == TransactionPendingInvalidStatus.COST_CENTER_DATA_NOT_FOUND){
  122 |             transactionItem.TxCostCenter = Math.random().toString(36).substring(2, 2 + 8);
  123 |         }
  124 |         if(pendingReason == TransactionPendingInvalidStatus.VAT_DATA_NOT_FOUND){
  125 |             transactionItem.VatCode = Math.random().toString(36).substring(2, 2 + 8);
  126 |         }
  127 |         if(pendingReason == TransactionPendingInvalidStatus.CHART_OF_ACCOUNT_NOT_FOUND){
  128 |             transactionItem.DebitCode = Math.random().toString(36).substring(2, 2 + 8);
  129 |         }
  130 |     }
  131 |     const setInvalidReason = async (debitTransactionItem: TransactionItemCsvDto, creditTransactionItem: TransactionItemCsvDto,
  132 |                                     invalidReason: string) => {
  133 |         if(invalidReason == TransactionPendingInvalidStatus.UNBALANCED_TRANSACTION){
  134 |             debitTransactionItem.AmountLcyDebit += 1000;
  135 |             debitTransactionItem.AmountFcyDebit += 1000;
  136 |         }
  137 |         if(invalidReason == TransactionPendingInvalidStatus.TX_INTERNAL_NUMBER_MUST_BE_PRESENT){
  138 |             debitTransactionItem.TxNumber = "";
  139 |             creditTransactionItem.TxNumber= "";
  140 |         }
  141 |         if(invalidReason == TransactionPendingInvalidStatus.ACCOUNT_CODE_DEBIT_IS_EMPTY){
  142 |             debitTransactionItem.DebitCode = "";
  143 |         }
  144 |     }
  145 |     const getTransactionType = async () => {
  146 |         const transactionTypeResponse = await (await reeveService(request))
  147 |             .getTransactionTypes(authToken);
> 148 |         expect(transactionTypeResponse.status()).toEqual(HttpStatusCodes.success);
      |                                                  ^ Error: expect(received).toEqual(expected) // deep equality
  149 |         const transactionTypes: TransactionTypeDto[] = await (transactionTypeResponse.json());
  150 |         const randomTxType = Math.floor(Math.random() * (transactionTypes.length - 1));
  151 |         return (transactionTypes[randomTxType].id)
  152 |     }
  153 | 
  154 |     const getEventCodes = async () => {
  155 |         const eventCodesResponse = await (await reeveService(request)).getEventCodes(authToken);
  156 |         expect(eventCodesResponse.status()).toEqual(HttpStatusCodes.success);
  157 |         const eventCodes: EventCodesDto[] = await (eventCodesResponse.json());
  158 |         const referenceCodes: ReferenceCodePair[] = eventCodes.map(eventCode => ({
  159 |             debitReferenceCode: eventCode.debitReferenceCode,
  160 |             creditReferenceCode: eventCode.creditReferenceCode
  161 |         }));
  162 |         return referenceCodes;
  163 |     }
  164 | 
  165 |     /**
  166 |      * Get two lists of accounts that has an event code
  167 |      * for the combination of debit and credit accounts
  168 |      * @param eventCodes array of organization's event codes
  169 |      *
  170 |      */
  171 |     const getDebitAndCreditAccounts = async (eventCodes: ReferenceCodePair[]) => {
  172 |         const chartOfAccounts: AccountRefCodePair[] = await getChartOfAccounts();
  173 |         let index = 0;
  174 |         let accountsMatch: boolean = false;
  175 |         let debitAccounts: AccountCodeAndNamePair[] | null;
  176 |         let creditAccounts: AccountCodeAndNamePair[] | null;
  177 |         while (accountsMatch == false) {
  178 |             if (eventCodes[index].debitReferenceCode != eventCodes[index].creditReferenceCode) {
  179 |                 debitAccounts = chartOfAccounts.filter(chartOfAccount =>
  180 |                     chartOfAccount.referenceCode === eventCodes[index].debitReferenceCode)
  181 |                     .map(chartOfAccount => ({
  182 |                         accountCode: chartOfAccount.accountCode,
  183 |                         accountName: chartOfAccount.accountName
  184 |                     }));
  185 |                 if (debitAccounts.length >= 1) {
  186 |                     creditAccounts = chartOfAccounts.filter(chartOfAccount =>
  187 |                         chartOfAccount.referenceCode === eventCodes[index].creditReferenceCode
  188 |                         && chartOfAccount.accountCode !== debitAccounts[0].accountCode)
  189 |                         .map(chartOfAccount => ({
  190 |                             accountCode: chartOfAccount.accountCode,
  191 |                             accountName: chartOfAccount.accountName
  192 |                         }));
  193 |                 }
  194 |                 if (creditAccounts != null) {
  195 |                     accountsMatch = true;
  196 |                 }
  197 |             }
  198 |             index++;
  199 |         }
  200 |         const debitAndCreditAccounts: DebitAndCreditAccounts = {
  201 |             debitAccounts: debitAccounts,
  202 |             creditAccounts: creditAccounts
  203 |         }
  204 |         return debitAndCreditAccounts
  205 |     }
  206 | 
  207 |     const getChartOfAccounts = async () => {
  208 |         const chartOfAccountsResponse = await (await reeveService(request)).getChartOfAccounts(authToken);
  209 |         expect(chartOfAccountsResponse.status()).toEqual(HttpStatusCodes.success);
  210 |         const chartOfAccounts: AccountRefCodePair[] = (await (chartOfAccountsResponse).json())
  211 |             .map(chartOfAccount => ({
  212 |                 accountCode: chartOfAccount.customerCode,
  213 |                 referenceCode: chartOfAccount.eventRefCode,
  214 |                 accountName: chartOfAccount.name
  215 |             }))
  216 |         return chartOfAccounts
  217 |     }
  218 | 
  219 |     const createTransactionItem = async (transactionItemCommonData: TransactionItemCsvDto, amount: string,
  220 |                                          isDebit: boolean, debitAndCreditAccounts: DebitAndCreditAccounts) => {
  221 |         let randomIndexDebit = Math.floor(Math.random() * debitAndCreditAccounts.debitAccounts.length)
  222 |         let randomIndexCredit = Math.floor(Math.random() * debitAndCreditAccounts.creditAccounts.length)
  223 |         const transactionItem: TransactionItemCsvDto = {
  224 |             TxNumber: transactionItemCommonData.TxNumber,
  225 |             TxDate: transactionItemCommonData.TxDate,
  226 |             TxType: transactionItemCommonData.TxType,
  227 |             FxRate: "1",
  228 |             AmountLcyDebit: "",
  229 |             AmountLcyCredit: "",
  230 |             AmountFcyDebit: "",
  231 |             AmountFcyCredit: "",
  232 |             DebitCode: "",
  233 |             DebitName: "",
  234 |             CreditCode: "",
  235 |             CreditName: "",
  236 |             ProjectCode: "",
  237 |             DocumentName: transactionItemCommonData.DocumentName,
  238 |             TxCurrency: "CHF",
  239 |             VatRate: "",
  240 |             VatCode: "",
  241 |             TxCostCenter: "",
  242 |             CounterParty: "",
  243 |             CounterpartyName: "",
  244 |         }
  245 |         if (isDebit) {
  246 |             transactionItem.AmountLcyDebit = amount;
  247 |             transactionItem.AmountFcyDebit = amount;
  248 |         } else {
```
package org.cardanofoundation.lob.app.accounting_reporting_core.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.model.AccountingCorePresentationViewService;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ExtractionRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.SearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.TransactionView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AccountingCoreResource {

    private final AccountingCorePresentationViewService accountingCorePresentationService;

    @Tag(name = "Transactions", description = "Transactions API")
    @PostMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listAllAction(@Valid @RequestBody SearchRequest body) {
        List<TransactionView> transactions = accountingCorePresentationService.allTransactions(body);

        return ResponseEntity.ok().body(transactions);
    }

    @Tag(name = "Transactions", description = "Transactions API")
    @GetMapping(value = "/transactions/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> transactionDetailSpecific(@Valid @PathVariable("transactionId") String transactionId) {

        val transactionEntity = accountingCorePresentationService.transactionDetailSpecific(transactionId);
        if (transactionEntity.isEmpty()) {
            val issue = Problem.builder()
                    .withTitle("TX_NOT_FOUND")
                    .withDetail(STR."Transaction with id: {\{transactionId}} could not be found")
                    .withStatus(Status.NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }

        return ResponseEntity.ok().body(transactionEntity);
    }

    @Tag(name = "Transactions", description = "Transactions API")
    @Operation(description = "Transaction types", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "[{\"id\":\"CardCharge\",\"title\":\"Card Charge\"},{\"id\":\"VendorBill\",\"title\":\"Vendor Bill\"},{\"id\":\"CardRefund\",\"title\":\"Card Refund\"},{\"id\":\"Journal\",\"title\":\"Journal\"},{\"id\":\"FxRevaluation\",\"title\":\"Fx Revaluation\"},{\"id\":\"Transfer\",\"title\":\"Transfer\"},{\"id\":\"CustomerPayment\",\"title\":\"Customer Payment\"},{\"id\":\"ExpenseReport\",\"title\":\"Expense Report\"},{\"id\":\"VendorPayment\",\"title\":\"Vendor Payment\"},{\"id\":\"BillCredit\",\"title\":\"Bill Credit\"}]"))}
            )
    })
    @GetMapping(value = "/transaction-types", produces = MediaType.APPLICATION_JSON_VALUE, name = "Transaction types")
    public ResponseEntity<?> transactionType() throws JSONException {

        JSONArray jsonArray = new JSONArray();

        for (TransactionType transactionType : TransactionType.values()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", transactionType);
            jsonObject.put("title", transactionType.name().replaceAll("(\\p{Lower})(\\p{Upper})", "$1 $2"));

            jsonArray.put(jsonObject);
        }

        return ResponseEntity.ok().body(jsonArray.toString());
    }

    @Tag(name = "Transactions", description = "Transactions API")
    @PostMapping(value = "/extraction", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Trigger the extraction from the ERP system(s)", responses = {
            @ApiResponse(content =
                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"event\": \"EXTRACTION\",\"message\":\"We have received your extraction request now. Please review imported transactions from the batch list.\"}"))},
                    responseCode = "202"
            )
    })
    public ResponseEntity<?> extractionTrigger(@Valid @RequestBody ExtractionRequest body) {

        accountingCorePresentationService.extractionTrigger(body);
        JSONObject response = new JSONObject()
                .put("event", "EXTRACTION")
                .put("message", "We have received your extraction request now. Please review imported transactions from the batch list.");

        return ResponseEntity
                .status(HttpStatusCode.valueOf(202))
                .body(response.toString());
    }

    @Tag(name = "Batchs", description = "Batchs API")
    @PostMapping(value = "/batchs", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Batch list",
            responses = {
                    @ApiResponse(content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "[\n{\n  \"id\": \"eb47142027c0788116d14723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04\",\n  \"createdAt\": \"2024-05-23T18:44:18.521920\",\n  \"updatedAt\": \"2024-05-23T18:44:18.521920\",\n  \"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\",\n  \"status\": \"CREATED\",\n  \"batchStatistics\": null,\n  \"transactions\": []\n},\n{\n  \"id\": \"e5087575b3e995aad6167bb14113ba5138b565ec691d9b67b87a1bb1ca82787d\",\n  \"createdAt\": \"2024-05-23T18:49:18.989285\",\n  \"updatedAt\": \"2024-05-23T18:49:22.455758\",\n  \"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\",\n  \"status\": \"FINISHED\",\n  \"batchStatistics\": {\n    \"totalTransactionsCount\": 1,\n    \"processedTransactionsCount\": 1,\n    \"failedTransactionsCount\": 1,\n    \"failedSourceERPTransactionsCount\": 1,\n    \"failedSourceLOBTransactionsCount\": 1,\n    \"approvedTransactionsCount\": 1,\n    \"approvedTransactionsDispatchCount\": 1,\n    \"dispatchedTransactionsCount\": 0,\n    \"completedTransactionsCount\": 0,\n    \"finalizedTransactionsCount\": 0\n  },\n  \"transactions\": []\n},\n{\n  \"id\": \"7ab24eb3999b81236202218430dd743fdf3bf76faeee567d2162796b26be34f1\",\n  \"createdAt\": \"2024-05-24T10:01:28.819673\",\n  \"updatedAt\": \"2024-05-24T10:01:32.729725\",\n  \"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\",\n  \"status\": \"PROCESSING\",\n  \"batchStatistics\": {\n    \"totalTransactionsCount\": 235,\n    \"processedTransactionsCount\": 234,\n    \"failedTransactionsCount\": 2,\n    \"failedSourceERPTransactionsCount\": 2,\n    \"failedSourceLOBTransactionsCount\": 2,\n    \"approvedTransactionsCount\": 234,\n    \"approvedTransactionsDispatchCount\": 234,\n    \"dispatchedTransactionsCount\": 0,\n    \"completedTransactionsCount\": 0,\n    \"finalizedTransactionsCount\": 0\n  },\n  \"transactions\": []\n}\n]\nResponse headers\n"))
                    }),
                    @ApiResponse(responseCode = "404", description = "Error: response status is 404", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{\"title\": \"BATCH_ORGANISATION_NOT_FOUND\",\"status\": 404,\"detail\": \"Batch with organization id: {organisationId} could not be found\"" +
                            "}"))})
            }
    )
    public ResponseEntity<?> listAllBatch(@Valid @RequestBody BatchSearchRequest body) {

        val batchs = accountingCorePresentationService.listAllBatch(body);

        if (batchs.isEmpty()) {
            val issue = Problem.builder()
                    .withTitle("BATCH_ORGANISATION_NOT_FOUND")
                    .withDetail("Batch with organization id: {" + body.getOrganisationId() + "} could not be found")
                    .withStatus(Status.NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }
        return ResponseEntity.ok().body(batchs);
    }

    @Tag(name = "Batchs", description = "Batchs API")
    @GetMapping(value = "/batchs/{batchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Batch detail",
            responses = {
                    @ApiResponse(content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{ \"id\": \"87d5540098b85d152c14bab1b1aabaef9ab178219a2e49079cd5d62492fdaa59\", \"createdAt\": \"2024-05-09T14:52:10.297132\", \"updatedAt\": \"2024-05-09T14:52:14.500029\", \"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\", \"batchStatistics\": { \"totalTransactionsCount\": 4, \"processedTransactionsCount\": 4, \"failedTransactionsCount\": 4, \"failedSourceERPTransactionsCount\": 4, \"failedSourceLOBTransactionsCount\": 4, \"approvedTransactionsCount\": 4, \"approvedTransactionsDispatchCount\": 4, \"dispatchedTransactionsCount\": 0, \"completedTransactionsCount\": 0, \"finalizedTransactionsCount\": 0 }, \"transactions\": [ { \"id\": \"5a5966c098b2883fb0141575f6fed1917d84be9ffa4475007351aeffb7e76cb3\", \"internalTransactionNumber\": \"JOURNAL133\", \"entryDate\": \"2022-12-31\", \"transactionType\": \"Journal\", \"validationStatus\": \"FAILED\", \"transactionApproved\": true, \"ledgerDispatchApproved\": true, \"items\": [ { \"id\": \"a0149a807165142ecf5f7f57fcef283841d974d10bcdcecf94c5ac5bb067e07b\", \"accountDebit\": { \"code\": \"1101410402\", \"refCode\": \"1100\", \"name\": \"Cash and Cash Equivalents : Banks and Third Parties : Sygnum Bank - USD CH09 8301\" }, \"accountCredit\": null, \"amountFcy\": 6010322.00, \"amountLcy\": 5560918.20 }, { \"id\": \"514a368bc5612133e4cc18512ecb873c4b47296f5acb84c3ce236e456de7580f\", \"accountDebit\": { \"code\": \"2406210100\", \"refCode\": \"3900\", \"name\": \"Equity : Total Retained Earnings : Opening Balance\" }, \"accountCredit\": null, \"amountFcy\": -6010322.00, \"amountLcy\": -5560918.20 } ], \"violations\": [ { \"type\": \"ERROR\", \"source\": \"LOB\", \"transactionItemId\": \" \", \"code\": \"JOURNAL_DUMMY_ACCOUNT_MISSING\", \"bag\": {} } ] }, { \"id\": \"e0316b37c08866b2d0c50733f2d11703d31ec0028a19194bc5f666eeeecca02e\", \"internalTransactionNumber\": \"EXPREPT260\", \"entryDate\": \"2023-1-13\", \"transactionType\": \"ExpenseReport\", \"validationStatus\": \"FAILED\", \"transactionApproved\": true, \"ledgerDispatchApproved\": true, \"items\": [ { \"id\": \"cbf490a6623f28c9c8a36f080c6363b61763209483de43d048d728a28a6480a4\", \"accountDebit\": { \"code\": \"2101110101\", \"refCode\": \"3100\", \"name\": \"Accounts Payable : Accounts Payable : Accounts Payble EUR\" }, \"accountCredit\": { \"code\": \"2101110101\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": -51.58, \"amountLcy\": -77.83 }, { \"id\": \"092ae5cb0c06ec9bc00d4adc9f2c29707bfa9df2ddfcc312ee625563d509b3f6\", \"accountDebit\": { \"code\": \"6313110100\", \"refCode\": \"7300\", \"name\": \"Total Other operating Expenses : Travel Expense : Air/Rail/Car Travel\" }, \"accountCredit\": { \"code\": \"2101110101\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 51.58, \"amountLcy\": 77.83 } ], \"violations\": [ { \"type\": \"ERROR\", \"source\": \"LOB\", \"transactionItemId\": \" \", \"code\": \"JOURNAL_DUMMY_ACCOUNT_MISSING\", \"bag\": {} } ] }, { \"id\": \"3224a828669d546a3df9e8900dac6173634c48c7c3d1d399838ebf6df134c14b\", \"internalTransactionNumber\": \"JOURNAL67\", \"entryDate\": \"2022-12-31\", \"transactionType\": \"Journal\", \"validationStatus\": \"FAILED\", \"transactionApproved\": true, \"ledgerDispatchApproved\": true, \"items\": [ { \"id\": \"8465590b55f67993663263b1300bb463f7436831bd44fb7441bd07bfcdf51bd8\", \"accountDebit\": { \"code\": \"1203148340\", \"refCode\": \"W100\", \"name\": \"Inventory : Wallets Adalite : Adalite Account 004 (Dev 1 / 004)\" }, \"accountCredit\": null, \"amountFcy\": 14588733.68, \"amountLcy\": 1218640.73 }, { \"id\": \"1788cd39bc6f0d61410d6cd7ccc7314ec29406e00719efde5e5f5b09495a2ecf\", \"accountDebit\": { \"code\": \"2406210100\", \"refCode\": \"3900\", \"name\": \"Equity : Total Retained Earnings : Opening Balance\" }, \"accountCredit\": null, \"amountFcy\": -14588733.68, \"amountLcy\": -1218640.73 } ], \"violations\": [ { \"type\": \"ERROR\", \"source\": \"LOB\", \"transactionItemId\": \" \", \"code\": \"JOURNAL_DUMMY_ACCOUNT_MISSING\", \"bag\": {} } ] }, { \"id\": \"13ae3992b8a85b3294a1c0cb87b91116c9e70c2b25857786ab9541dad2148a73\", \"internalTransactionNumber\": \"CARDCHRG46\", \"entryDate\": \"2023-1-1\", \"transactionType\": \"CardCharge\", \"validationStatus\": \"FAILED\", \"transactionApproved\": true, \"ledgerDispatchApproved\": true, \"items\": [ { \"id\": \"a329f72c4e8287556dc656b611273ba8f71e22b52c106204079099a9c1d91c8d\", \"accountDebit\": { \"code\": \"2102110100\", \"refCode\": \"3100\", \"name\": \"UBS Corporate Credit Cards\" }, \"accountCredit\": { \"code\": \"2102110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": -10146.54, \"amountLcy\": -10146.54 }, { \"id\": \"1c1a57c2f02941df5f0eba09cab999532f24fbe44605a1179def0a65da882aef\", \"accountDebit\": { \"code\": \"6317110100\", \"refCode\": \"7800\", \"name\": \"Total Other operating Expenses : IT Expenses : IT Subscriptions\" }, \"accountCredit\": { \"code\": \"2102110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 9421.11, \"amountLcy\": 9421.11 }, { \"id\": \"cae2e2435b2098bebae7815ad886546e8e46a55ff1abb5547d9e20e25a931ebc\", \"accountDebit\": { \"code\": \"1206340100\", \"refCode\": \"1600\", \"name\": \"Other Current Assets : 120634 Swiss VAT on goods and services -7.7%\" }, \"accountCredit\": { \"code\": \"2102110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 725.43, \"amountLcy\": 725.43 } ], \"violations\": [ { \"type\": \"ERROR\", \"source\": \"LOB\", \"transactionItemId\": \" \", \"code\": \"JOURNAL_DUMMY_ACCOUNT_MISSING\", \"bag\": {} } ] } ] } \n"))
                    }),
                    @ApiResponse(responseCode = "404", description = "Error: response status is 404", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{\"title\": \"BATCH_NOT_FOUND\",\"status\": 404,\"detail\": \"Batch with id: {batchId} could not be found\"" +
                            "}"))})
            }
    )

    public ResponseEntity<?> batchDetail(@Valid @PathVariable("batchId") String batchId) {

        val txBatchM = accountingCorePresentationService.batchDetail(batchId);
        if (txBatchM.isEmpty()) {
            val issue = Problem.builder()
                    .withTitle("BATCH_NOT_FOUND")
                    .withDetail(STR."Batch with id: {\{batchId}} could not be found")
                    .withStatus(Status.NOT_FOUND)
                    .build();

            return ResponseEntity.status(issue.getStatus().getStatusCode()).body(issue);
        }

        return ResponseEntity.ok().

                body(txBatchM.orElseThrow());
    }

}

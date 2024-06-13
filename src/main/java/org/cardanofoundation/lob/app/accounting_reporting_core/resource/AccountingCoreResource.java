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
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "[{\n\"id\": \"f346cc734fe3008ac5fc19b41c7c779690bc69320c97f3a5618554159802fe12\",\n\"createdAt\": \"2024-06-11T12:09:52.962632\",\n\"updatedAt\": \"2024-06-11T12:09:58.707360\",\n\"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\",\n\"status\": \"PROCESSING\",\n\"batchStatistics\": {\n\"totalTransactionsCount\": 235,\n\"processedTransactionsCount\": 235,\n\"failedTransactionsCount\": 2,\n\"failedSourceERPTransactionsCount\": 2,\n\"failedSourceLOBTransactionsCount\": 2,\n\"approvedTransactionsCount\": 235,\n\"approvedTransactionsDispatchCount\": 235,\n\"dispatchedTransactionsCount\": 0,\n\"completedTransactionsCount\": 0,\n\"finalizedTransactionsCount\": 0\n },\n\"filteringParameters\": {\n\"transactionTypes\": [\n\"CardCharge\",\n\"VendorBill\",\n\"VendorPayment\",\n\"BillCredit\"\n ],\n\"from\": \"2013-01-02\",\n\"to\": \"2024-05-01\",\n\"accountingPeriodFrom\": \"2021-06\",\n\"accountingPeriodTo\": \"2024-06\",\n\"transactionNumbers\": [\n\"CARDCH565\",\n\"CARDCHRG159\",\n\"CARDHY777\",\n\"VENDBIL119\"\n ]\n },\n\"transactions\": []\n  }]"))
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
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{ \"id\": \"f346cc734fe3008ac5fc19b41c7c779690bc69320c97f3a5618554159802fe12\", \"createdAt\": \"2024-06-11T12:09:52.962632\", \"updatedAt\": \"2024-06-11T12:09:58.707360\", \"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\", \"status\": \"PROCESSING\", \"batchStatistics\": { \"totalTransactionsCount\": 235, \"processedTransactionsCount\": 235, \"failedTransactionsCount\": 2, \"failedSourceERPTransactionsCount\": 2, \"failedSourceLOBTransactionsCount\": 2, \"approvedTransactionsCount\": 235, \"approvedTransactionsDispatchCount\": 235, \"dispatchedTransactionsCount\": 0, \"completedTransactionsCount\": 0, \"finalizedTransactionsCount\": 0 }, \"filteringParameters\": { \"transactionTypes\": [ \"CardCharge\", \"VendorBill\", \"VendorPayment\", \"BillCredit\" ], \"from\": \"2013-01-02\", \"to\": \"2024-05-01\", \"accountingPeriodFrom\": \"2021-06\", \"accountingPeriodTo\": \"2024-06\", \"transactionNumbers\": [] }, \"transactions\": [ { \"id\": \"a27e8fa2588abea745e7ea7bb5716ac0e90dcd4bc31568f4de2bf16c6a1004bf\", \"internalTransactionNumber\": \"CARDCHRG40\", \"entryDate\": \"2023-01-01\", \"transactionType\": \"CardCharge\", \"validationStatus\": \"VALIDATED\", \"transactionApproved\": true, \"ledgerDispatchApproved\": true, \"items\": [ { \"id\": \"db0c69b2405cfcd465414b457d8b783b883c4c775060d9ac270ddf5a2ae7228a\", \"accountDebit\": { \"code\": \"6317110100\", \"refCode\": \"7800\", \"name\": \"Total Other operating Expenses : IT Expenses : IT Subscriptions\" }, \"accountCredit\": { \"code\": \"2102110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 12.3, \"amountLcy\": 12.3 }, { \"id\": \"c8582299ee43ac7914411ad9f863afa33694ed69c8c0b5488b4b5773f443ea68\", \"accountDebit\": { \"code\": \"1206340100\", \"refCode\": \"1600\", \"name\": \"Other Current Assets : 120634 Swiss VAT on goods and services -7.7%\" }, \"accountCredit\": { \"code\": \"2102110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 0.95, \"amountLcy\": 0.95 } ], \"violations\": [], \"status\": \"OK\" }, { \"id\": \"0acc08b26a668f23516d947852b8277fb920d2a4b39242bfbf0f0b04d3bc8f7f\", \"internalTransactionNumber\": \"VENDBILL72\", \"entryDate\": \"2023-01-09\", \"transactionType\": \"VendorBill\", \"validationStatus\": \"VALIDATED\", \"transactionApproved\": true, \"ledgerDispatchApproved\": true, \"items\": [ { \"id\": \"ff0418b83258caae59b31d692d4b4d295643183a011a5349c644163902597c45\", \"accountDebit\": { \"code\": \"6314110100\", \"refCode\": \"7400\", \"name\": \"Total Other operating Expenses : Total Professional services : Accounting & Financial services\" }, \"accountCredit\": { \"code\": \"2101110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 511.99, \"amountLcy\": 511.99 }, { \"id\": \"69ed7963e4c7d28ab8077451dbd0d3bb5b22a604d79e05bba916dc22bbe6f8f3\", \"accountDebit\": { \"code\": \"6314110100\", \"refCode\": \"7400\", \"name\": \"Total Other operating Expenses : Total Professional services : Accounting & Financial services\" }, \"accountCredit\": { \"code\": \"2101110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 314, \"amountLcy\": 314 }, { \"id\": \"33c4a70a27aa74ea1c1621d02001f2b5c11cc7748b5c5ea79a6b673c519b2fde\", \"accountDebit\": { \"code\": \"6314110100\", \"refCode\": \"7400\", \"name\": \"Total Other operating Expenses : Total Professional services : Accounting & Financial services\" }, \"accountCredit\": { \"code\": \"2101110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 121.5, \"amountLcy\": 121.5 }, { \"id\": \"19f154d93e130faefcbb9b812d6bff7d3103aba5b85eba2834ddcc0a0c55f31e\", \"accountDebit\": { \"code\": \"1206340100\", \"refCode\": \"1600\", \"name\": \"Other Current Assets : 120634 Swiss VAT on goods and services -7.7%\" }, \"accountCredit\": { \"code\": \"2101110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 106.5, \"amountLcy\": 106.5 } ], \"violations\": [], \"status\": \"OK\" },{ \"id\": \"7e9e8bcbb38a283b41eab57add98278561ab51d23a16f3e3baf3daa461b84ab4\", \"internalTransactionNumber\": \"CARDCHRG159\", \"entryDate\": \"2023-07-04\", \"transactionType\": \"CardCharge\", \"validationStatus\": \"FAILED\", \"transactionApproved\": true, \"ledgerDispatchApproved\": true, \"items\": [ { \"id\": \"3e212212c5e7639134ea08a39d1b7e3da27fe65ac0da6897f606c15dbaebfc13\", \"accountDebit\": { \"code\": \"1206320100\", \"refCode\": \"1600\", \"name\": \"Other Current Assets : 120632 VAT Input Tax - Cost of Materials\" }, \"accountCredit\": { \"code\": \"2102110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 11.17, \"amountLcy\": 11.17 }, { \"id\": \"5d178b050e2b16ee04547c16f71708f430d3137871d2188e098223191a58114d\", \"accountDebit\": { \"code\": \"6317110100\", \"refCode\": \"7800\", \"name\": \"Total Other operating Expenses : IT Expenses : IT Subscriptions\" }, \"accountCredit\": { \"code\": \"2102110100\", \"refCode\": \"3100\", \"name\": null }, \"amountFcy\": 145, \"amountLcy\": 145 } ], \"violations\": [ { \"type\": \"ERROR\", \"source\": \"ERP\", \"transactionItemId\": \"3e212212c5e7639134ea08a39d1b7e3da27fe65ac0da6897f606c15dbaebfc13\", \"code\": \"DOCUMENT_MUST_BE_PRESENT\", \"bag\": { \"transactionNumber\": \"CARDCHRG159\" } }, { \"type\": \"ERROR\", \"source\": \"ERP\", \"transactionItemId\": \"5d178b050e2b16ee04547c16f71708f430d3137871d2188e098223191a58114d\", \"code\": \"DOCUMENT_MUST_BE_PRESENT\", \"bag\": { \"transactionNumber\": \"CARDCHRG159\" } } ], \"status\": \"FAIL\" } ]} \n"))
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

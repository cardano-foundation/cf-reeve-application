package org.cardanofoundation.lob.app;

import io.restassured.http.Header;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.scribe.model.SignatureType.Header;

class AccountingCoreResourceTest extends WebBaseIntegrationTest {

    @Test
    void testListAllTransactions() {
        val inputRequestJson = """
                {
                  "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                  "transactionType": [
                    "CardCharge",
                    "VendorBill",
                    "CardRefund",
                    "Journal",
                    "FxRevaluation",
                    "Transfer",
                    "CustomerPayment",
                    "ExpenseReport",
                    "VendorPayment",
                    "BillCredit"
                  ],
                    "status": ["VALIDATED","FAILED"]
                }""";

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(200)
                //.body("id", containsString(expectedUpdatedAt))
                .body("id[0]", equalTo("e86d9c787f7b4f5e4000ada66e267d4e4ff36a98343833f725a8f9933d5a4031"));
    }

    // TODO this fails for me since localisation should not be part of the answer:
//    ```
//    java.lang.AssertionError: 1 expectation failed.
//    Response body doesn't match expectation.
//    Expected: "{\"violations\":[{\"field\":\"organisationId\",\"message\":\"must not be blank\"}],\"type\":\"https://zalando.github.io/problem/constraint-violation\",\"status\":400,\"title\":\"Constraint Violation\"}"
//    Actual: {"violations":[{"field":"organisationId","message":"nie może być odstępem"}],"type":"https://zalando.github.io/problem/constraint-violation","status":400,"title":"Constraint Violation"}
//
//    at java.base/jdk.internal.reflect.DirectConstructorHandleAccessor.newInstance(DirectConstructorHandleAccessor.java:62)
//    ```

    @Test
    void testListAllTransactionsNoOrgnanisationId() {
        val inputRequestJson = """
                {
                  "organisationId": "",
                  "transactionType": [
                    "CardCharge",
                    "VendorBill",
                    "CardRefund",
                    "Journal",
                    "FxRevaluation",
                    "Transfer",
                    "CustomerPayment",
                    "ExpenseReport",
                    "VendorPayment",
                    "BillCredit"
                  ],
                    "status": ["VALIDATED","FAILED"]
                }""";

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(400)
                .body(equalTo("{\"violations\":[{\"field\":\"organisationId\",\"message\":\"Organisation Id is mandatory and must not be blank or null.\"}],\"type\":\"https://zalando.github.io/problem/constraint-violation\",\"status\":400,\"title\":\"Constraint Violation\"}"));
    }

    @Test
    void testExtractionTrigger() {
        val inputRequestJson = """
                {
                  "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                  "dateFrom": "2023-01-01",
                  "dateTo": "2024-05-01",
                  "transactionType": [
                    "CardCharge",
                    "VendorBill",
                    "CardRefund",
                    "Journal",
                    "FxRevaluation",
                    "Transfer",
                    "CustomerPayment",
                    "ExpenseReport",
                    "VendorPayment",
                    "BillCredit"
                  ],
                  "transactionNumbers": [
                    "CARDCH565",
                    "CARDHY777",
                    "CARDCHRG159",
                    "VENDBIL119"
                  ]
                }""";

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/extraction")
                .then()
                .statusCode(202)
                .body("event", equalTo("EXTRACTION"))
                .body("message", equalTo("We have received your extraction request now. Please review imported transactions from the batch list."));
    }

    @Test
    void testExtractionTriggerFailDueToToManyTransactionNumbers() {
        val numbers = TxNumbersGenerator.generateUniqueTransactionNumbers(601);
        // turn into json string as array including opening and ending brackets
        val transactionNumbersJson = numbers.stream().collect(Collectors.joining("\",\"", "[\"", "\"]"));

        val inputRequestJson = STR."""
                                    {
                                      "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                                      "dateFrom": "2023-01-01",
                                      "dateTo": "2024-05-01",
                                      "transactionType": [
                                        "CardCharge",
                                        "VendorBill",
                                        "CardRefund",
                                        "Journal",
                                        "FxRevaluation",
                                        "Transfer",
                                        "CustomerPayment",
                                        "ExpenseReport",
                                        "VendorPayment",
                                        "BillCredit"
                                      ],
                                      "transactionNumbers": \{transactionNumbersJson}
                }""";

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/extraction")
                .then()
                .statusCode(400)
                .body("title", equalTo("TOO_MANY_TRANSACTIONS"))
                .body("detail", equalTo("Too many transactions requested, maximum is 600"));
    }

    @Test
    void testListAllActionWrongDate() {
        val inputRequestJson = """
                {
                  "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                  "dateFrom": "2003-01-01",
                  "dateTo": "2024-05-01",
                  "transactionType": [
                    "CardCharge",
                    "VendorBill",
                    "CardRefund",
                    "Journal",
                    "FxRevaluation",
                    "Transfer",
                    "CustomerPayment",
                    "ExpenseReport",
                    "VendorPayment",
                    "BillCredit"
                  ],
                  "transactionNumbers": [
                    "CARDCH565",
                    "CARDHY777",
                    "CARDCHRG159",
                    "VENDBIL119"
                  ]
                }""";

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/extraction")
                .then()
                .statusCode(400)
                .body("title", equalTo("ORGANISATION_DATE_MISMATCH"))
                .body("detail", startsWith("Date range must be within the accounting period:"));
    }

    @Test
    void testListAllAction() {
        val inputRequestJson = """
    {
        "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
        "status": [
            "VALIDATED"
        ]
    }
    """;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("e86d9c787f7b4f5e4000ada66e267d4e4ff36a98343833f725a8f9933d5a4031"));
    }

    @Test
    void testTransactionType() {
        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/transaction-types")
                .then()
                .statusCode(200)
                .body("find { it.title == 'Card Refund' }.id", equalTo("CardRefund"))
                .body("find { it.title == 'Card Charge' }.id", equalTo("CardCharge"))
                .body("find { it.title == 'Vendor Bill' }.id", equalTo("VendorBill"))
                .body("find { it.title == 'Customer Payment' }.id", equalTo("CustomerPayment"))
                .body("find { it.title == 'Transfer' }.id", equalTo("Transfer"))
                .body("find { it.title == 'Vendor Payment' }.id", equalTo("VendorPayment"))
                .body("find { it.title == 'Journal' }.id", equalTo("Journal"))
                .body("find { it.title == 'Fx Revaluation' }.id", equalTo("FxRevaluation"))
                .body("find { it.title == 'Bill Credit' }.id", equalTo("BillCredit"))
                .body("find { it.title == 'Expense Report' }.id", equalTo("ExpenseReport"));
    }

    @Test
    void testRejectionReasons() {
        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/rejection-reasons")
                .then()
                .statusCode(200)
                .body(containsString("INCORRECT_AMOUNT"))
                .body(containsString("INCORRECT_COST_CENTER"))
                .body(containsString("INCORRECT_PROJECT"))
                .body(containsString("INCORRECT_CURRENCY"))
                .body(containsString("INCORRECT_VAT_CODE"))
                .body(containsString("REVIEW_PARENT_COST_CENTER"))
                .body(containsString("REVIEW_PARENT_PROJECT_CODE"));
    }

    @Test
    void testListAllBatch() {
        val inputRequestJson = """
                {
                    "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"
                }
                """;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/batches")
                .then()
                .statusCode(200)
                .body("batchs.id[0]", containsString("TEST_ReadyToPublish2_816d14723a4ab4a67636a7d63dc6f7adf61aba32c04"))
                .body("batchs.createdAt[0]", containsString("2024-08-18"))
                .body("batchs.updatedAt[0]", containsString("2024-08-18"))
                .body("batchs.organisationId[0]", containsString("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
                .body("total", equalTo(13));
    }

    @Test
    void testListAllBatchPending() {
        val inputRequestJson = """
    {
        "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
        "batchStatistics": [
            "PENDING"
        ]
    }
    """;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/batches")
                .then()
                .statusCode(200)
                .body("batchs.id[0]", containsString("TEST_Rejection_b6x90wedd2d9e814b96436029a8ca22440f65c64ef236459e"))
                .body("batchs.createdAt[0]", containsString("2024-07-17"))
                .body("batchs.updatedAt[0]", containsString("2024-08-16"))
                .body("batchs.organisationId[0]", containsString("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
                .body("batchs.batchStatistics[0].invalid", equalTo(0))
                .body("batchs.batchStatistics[0].pending", equalTo(1))
                .body("batchs.batchStatistics[0].approve", equalTo(0))
                .body("batchs.batchStatistics[0].publish", equalTo(0))
                .body("batchs.batchStatistics[0].published", equalTo(0))
                .body("batchs.batchStatistics[0].total", equalTo(1))
                .body("total", equalTo(5));
    }

    @Test
    void testListAllBatchInvalid() {
        val inputRequestJson = """
    {
        "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
        "batchStatistics": [
            "INVALID"
        ]
    }
    """;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/batches")
                .then()
                .statusCode(200)
                .body("batchs.id[0]", containsString("TEST_Invalid_88116d14723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04"))
                .body("batchs.createdAt[0]", containsString("2024-01-15"))
                .body("batchs.updatedAt[0]", containsString("2024-01-15"))
                .body("batchs.organisationId[0]", containsString("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
                .body("batchs.batchStatistics[0].invalid", equalTo(1))
                .body("batchs.batchStatistics[0].pending", equalTo(0))
                .body("batchs.batchStatistics[0].approve", equalTo(0))
                .body("batchs.batchStatistics[0].publish", equalTo(0))
                .body("batchs.batchStatistics[0].published", equalTo(0))
                .body("batchs.batchStatistics[0].total", equalTo(1))
                .body("total", equalTo(2));

    }

    @Test
    void testListAllBatchApprove() {
        val inputRequestJson = """
    {
        "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
        "batchStatistics": [
            "APPROVE"
        ]
    }
    """;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/batches")
                .then()
                .statusCode(200)
                .body("batchs.id[0]", containsString("TEST_ReadyToApprove_b6dd2d9e814b96436029a8ca22440f65c64ef236459e"))
                .body("batchs.createdAt[0]", containsString("2024-08-16"))
                .body("batchs.updatedAt[0]", containsString("2024-08-16"))
                .body("batchs.organisationId[0]", containsString("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
                .body("batchs.batchStatistics[0].invalid", equalTo(0))
                .body("batchs.batchStatistics[0].pending", equalTo(0))
                .body("batchs.batchStatistics[0].approve", equalTo(3))
                .body("batchs.batchStatistics[0].publish", equalTo(0))
                .body("batchs.batchStatistics[0].published", equalTo(0))
                .body("batchs.batchStatistics[0].total", equalTo(3))
                .body("total", equalTo(5));

    }

    @Test
    void testListAllBatchPublish() {
        val inputRequestJson = """
    {
        "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
        "batchStatistics": [
            "PUBLISH"
        ]
    }
    """;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/batches")
                .then()
                .statusCode(200)
                .body("batchs.id[0]", containsString("TEST_ReadyToPublish2_816d14723a4ab4a67636a7d63dc6f7adf61aba32c04"))
                .body("batchs.createdAt[0]", containsString("2024-08-18"))
                .body("batchs.updatedAt[0]", containsString("2024-08-18"))
                .body("batchs.organisationId[0]", containsString("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
                .body("batchs.batchStatistics[0].invalid", equalTo(0))
                .body("batchs.batchStatistics[0].pending", equalTo(0))
                .body("batchs.batchStatistics[0].approve", equalTo(0))
                .body("batchs.batchStatistics[0].publish", equalTo(3))
                .body("batchs.batchStatistics[0].published", equalTo(0))
                .body("batchs.batchStatistics[0].total", equalTo(3))
                .body("total", equalTo(5));
    }

    @Test
    void testListAllBatchPublished() {
        val inputRequestJson = """
    {
        "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
        "batchStatistics": [
            "PUBLISHED"
        ]
    }
    """;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/batches")
                .then()
                .statusCode(200)
                .body("batchs.id[0]", containsString("TEST_Published_345723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04123"))
                .body("batchs.createdAt[0]", containsString("2024-07-17"))
                .body("batchs.updatedAt[0]", containsString("2024-07-17"))
                .body("batchs.organisationId[0]", containsString("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
                .body("batchs.batchStatistics[0].invalid", equalTo(0))
                .body("batchs.batchStatistics[0].pending", equalTo(0))
                .body("batchs.batchStatistics[0].approve", equalTo(0))
                .body("batchs.batchStatistics[0].publish", equalTo(2))
                .body("batchs.batchStatistics[0].published", equalTo(1))
                .body("batchs.batchStatistics[0].total", equalTo(3))
                .body("total", equalTo(1));
    }

    @Test
    void testListAllBatchByTime() {
        val inputRequestJson = """
    {
        "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
        "from": "2024-06-13",
        "to": "2024-06-13"
    }
    """;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/batches")
                .then()
                .statusCode(200)
                .body("batchs.id[0]", containsString("TEST_ReadyToPublish_816d14723a4ab4a67636a7d63dc6f7adf61aba32c041"))
                .body("batchs.createdAt[0]", containsString("2024-06-13"))
                .body("batchs.updatedAt[0]", containsString("2024-06-13"))
                .body("batchs.organisationId[0]", containsString("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
                .body("batchs.batchStatistics[0].invalid", equalTo(0))
                .body("batchs.batchStatistics[0].pending", equalTo(0))
                .body("batchs.batchStatistics[0].approve", equalTo(0))
                .body("batchs.batchStatistics[0].publish", equalTo(2))
                .body("batchs.batchStatistics[0].published", equalTo(0))
                .body("batchs.batchStatistics[0].total", equalTo(2))
                .body("total", equalTo(1));
    }

    @Test
    void testListAllBatchNull() {
        val inputRequestJson = """
    {
        "organisationId": "65f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"
    }
    """;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputRequestJson)
                .when()
                .post("/api/batches")
                .then()
                .statusCode(200);
        //.body("title", equalTo("BATCH_ORGANISATION_NOT_FOUND"))
        //.body("detail", equalTo("Batch with organization id: {65f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94} could not be found"))
    }

    @Test
    void testListAllBatchNoBody() {
        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .post("/api/batches")
                .then()
                .statusCode(400)
                .body("title", equalTo("Bad Request"));
    }

    @Test
    void testListAllBatchDetail() {
        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/batches/TEST_ReadyToApprove_b6dd2d9e814b96436029a8ca22440f65c64ef236459e")
                .then()
                .statusCode(200)
                .body("id", equalTo("TEST_ReadyToApprove_b6dd2d9e814b96436029a8ca22440f65c64ef236459e"))
                .body("organisationId", equalTo("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"));
    }

    @Test
    void testListAllBatchDetailNull() {
        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/batches/fb47142027c0788116d14723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04")
                .then()
                .statusCode(404)
                .body("title", equalTo("BATCH_NOT_FOUND"))
                .body("detail", equalTo("Batch with id: {fb47142027c0788116d14723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04} could not be found"));
    }

    @Test
    void testListAllBatchReprocess() {
        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/batches/reprocess/TEST_ReadyToApprove_b6dd2d9e814b96436029a8ca22440f65c64ef236459e")
                .then()
                .statusCode(200)
                .body("batchId", equalTo("TEST_ReadyToApprove_b6dd2d9e814b96436029a8ca22440f65c64ef236459e"))
                .body("success", equalTo(true));
    }

    @Test
    void testListAllBatchReprocessNoExist() {
        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/batches/reprocess/fake")
                .then()
                .statusCode(200)
                .body("batchId", equalTo("fake"))
                .body("success", equalTo(false));
    }

}

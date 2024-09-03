package org.cardanofoundation.lob.app;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;


class AccountingCoreResourceTest extends WebBaseIntegrationTest {

    @Test
    void testListAllTransactions() throws Exception {
        String myJson = "{\n" +
                "  \"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\",\n" +
                "  \"transactionType\": [\n" +
                "    \"CardCharge\",\n" +
                "    \"VendorBill\",\n" +
                "    \"CardRefund\",\n" +
                "    \"Journal\",\n" +
                "    \"FxRevaluation\",\n" +
                "    \"Transfer\",\n" +
                "    \"CustomerPayment\",\n" +
                "    \"ExpenseReport\",\n" +
                "    \"VendorPayment\",\n" +
                "    \"BillCredit\"\n" +
                "  ],\n" +
                "    \"status\": [\"VALIDATED\",\"FAILED\"]\n" +
                "}";

        given()
                .contentType("application/json")
                .body(myJson)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(200)
                //.body("id", containsString(expectedUpdatedAt))
                .body("id[0]", equalTo("ReadyToPublish_2_c2b0d6e504aadf32d573602b9cff433f703b6e0618fa630"))

        ;

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
    void testListAllTransactionsNoOrgnanisationId() throws Exception {
        String myJson = "{\n" +
                "  \"organisationId\": \"\",\n" +
                "  \"transactionType\": [\n" +
                "    \"CardCharge\",\n" +
                "    \"VendorBill\",\n" +
                "    \"CardRefund\",\n" +
                "    \"Journal\",\n" +
                "    \"FxRevaluation\",\n" +
                "    \"Transfer\",\n" +
                "    \"CustomerPayment\",\n" +
                "    \"ExpenseReport\",\n" +
                "    \"VendorPayment\",\n" +
                "    \"BillCredit\"\n" +
                "  ],\n" +
                "    \"status\": [\"VALIDATED\",\"FAILED\"]\n" +
                "}";

        given()
                .contentType("application/json")
                .body(myJson)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(400)
                .body(equalTo("{\"violations\":[{\"field\":\"organisationId\",\"message\":\"must not be blank\"}],\"type\":\"https://zalando.github.io/problem/constraint-violation\",\"status\":400,\"title\":\"Constraint Violation\"}"))
        ;
    }

    @Test
    void testListAllAction() throws Exception {
        String myJson = "{\n" +
                "  \"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\",\n" +
                "  \"dateFrom\": \"2023-01-01\",\n" +
                "  \"dateTo\": \"2024-05-01\",\n" +
                "  \"transactionType\": [\n" +
                "    \"CardCharge\",\n" +
                "    \"VendorBill\",\n" +
                "    \"CardRefund\",\n" +
                "    \"Journal\",\n" +
                "    \"FxRevaluation\",\n" +
                "    \"Transfer\",\n" +
                "    \"CustomerPayment\",\n" +
                "    \"ExpenseReport\",\n" +
                "    \"VendorPayment\",\n" +
                "    \"BillCredit\"\n" +
                "  ],\n" +
                "  \"transactionNumbers\": [\n" +
                "    \"CARDCH565\",\n" +
                "    \"CARDHY777\",\n" +
                "    \"CARDCHRG159\",\n" +
                "    \"VENDBIL119\"\n" +
                "  ]\n" +
                "}";

        given()
                .contentType("application/json")
                .body(myJson)
                .when()
                .post("/api/extraction")
                .then()
                .statusCode(202)
                .body("event",equalTo("EXTRACTION"))
                .body("message",equalTo("We have received your extraction request now. Please review imported transactions from the batch list."))
                ;

    }

    @Test
    void testListAllActionWrongDate() throws Exception {
        String myJson = "{\n" +
                "  \"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\",\n" +
                "  \"dateFrom\": \"2013-01-01\",\n" +
                "  \"dateTo\": \"2024-05-01\",\n" +
                "  \"transactionType\": [\n" +
                "    \"CardCharge\",\n" +
                "    \"VendorBill\",\n" +
                "    \"CardRefund\",\n" +
                "    \"Journal\",\n" +
                "    \"FxRevaluation\",\n" +
                "    \"Transfer\",\n" +
                "    \"CustomerPayment\",\n" +
                "    \"ExpenseReport\",\n" +
                "    \"VendorPayment\",\n" +
                "    \"BillCredit\"\n" +
                "  ],\n" +
                "  \"transactionNumbers\": [\n" +
                "    \"CARDCH565\",\n" +
                "    \"CARDHY777\",\n" +
                "    \"CARDCHRG159\",\n" +
                "    \"VENDBIL119\"\n" +
                "  ]\n" +
                "}";

        given()
                .contentType("application/json")
                .body(myJson)
                .when()
                .post("/api/extraction")
                .then()
                .statusCode(404)
                .body("title", equalTo("ORGANISATION_DATE_MISMATCH"))
                .body("detail", equalTo("the requested data is outside of accounting period for 75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
        ;

    }

    @Test
    void testExtractionTrigger() throws Exception {
        String myJson = "{\"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\",\"status\": [\"VALIDATED\"]}";

        given()
                .contentType("application/json")
                .body(myJson)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("ReadyToPublish_2_c2b0d6e504aadf32d573602b9cff433f703b6e0618fa630"))

        ;

    }

    @Test
    void testTransactionType() throws Exception {

        ValidatableResponse este = given()
                .contentType("application/json")
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
                .body("find { it.title == 'Expense Report' }.id", equalTo("ExpenseReport"))
                ;

    }

    @Test
    void testListAllBatch() {
        String myJson = "{\"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\"}";
        String expectedCreatedAt = LocalDate.now().toString();
        String expectedUpdatedAt = LocalDate.now().toString();
        String expectedResponseBody = "[{\"id\":\"TEST_Invalid_88116d14723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04\",\"organisationId\":\"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\"}]";

        given()
                .contentType("application/json")
                .body(myJson)
                .when()
                .post("/api/batchs")
                .then()
                .statusCode(200)
                .body("batchs.id[0]", containsString("TEST_Invalid_88116d14723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04"))
                .body("batchs.createdAt[0]", containsString(expectedCreatedAt))
                .body("batchs.updatedAt[0]", containsString(expectedUpdatedAt))
                .body("batchs.organisationId[0]", containsString("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"));

        ;
    }

    @Test
    void testListAllBatchNull() {
        String myJson = "{\"organisationId\": \"65f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\"}";

        given()
                .contentType("application/json")
                .body(myJson)
                .when()
                .post("/api/batchs")
                .then()
                .statusCode(200)
                //.body("title", equalTo("BATCH_ORGANISATION_NOT_FOUND"))
                //.body("detail", equalTo("Batch with organization id: {65f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94} could not be found"))
        ;
    }

    @Test
    void testListAllBatchNoBody() {


        given()
                .contentType("application/json")
                .when()
                .post("/api/batchs")
                .then()
                .statusCode(400)
                .body("title", equalTo("Bad Request"))
        ;
    }

    @Test
    void testListAllBatchDetail() {

        given()
                .contentType("application/json")
                .when()
                .get("/api/batchs/TEST_ReadyToApprove_b6dd2d9e814b96436029a8ca22440f65c64ef236459e")
                .then()
                .statusCode(200)
                .body("id", equalTo("TEST_ReadyToApprove_b6dd2d9e814b96436029a8ca22440f65c64ef236459e"))
                .body("organisationId", equalTo("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))

        ;
    }
    @Test
    void testListAllBatchDetailNull() {

        given()
                .contentType("application/json")
                .when()
                .get("/api/batchs/fb47142027c0788116d14723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04")
                .then()
                .statusCode(404)
                .body("title", equalTo("BATCH_NOT_FOUND"))
                .body("detail", equalTo("Batch with id: {fb47142027c0788116d14723a4ab4a67636a7d6463d84f0c6f7adf61aba32c04} could not be found"))
        ;
    }
}
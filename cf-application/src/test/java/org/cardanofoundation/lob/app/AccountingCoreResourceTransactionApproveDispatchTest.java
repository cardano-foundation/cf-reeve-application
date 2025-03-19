package org.cardanofoundation.lob.app;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;


class AccountingCoreResourceTransactionApproveDispatchTest extends WebBaseIntegrationTest {

    @Test
    @Order(1)
    void testApproveDispatchTransaction() {

        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/All_test_ready_to_publish_27af5261ab51d23a16f3e3baf3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("PUBLISH"));


        given()
                .contentType("application/json")
                .body("{\n" +
                        "  \"organisationId\": \"dummy-organisation\",\n" +
                        "  \"transactionIds\": [\n" +
                        "    {\n" +
                        "      \"id\": \"All_test_ready_to_publish_27af5261ab51d23a16f3e3baf3daa461b84ab4\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .when()
                .post("/api/transactions/publish")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("All_test_ready_to_publish_27af5261ab51d23a16f3e3baf3daa461b84ab4"))
                .body("success[0]", equalTo(true))
        ;
        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/All_test_ready_to_publish_27af5261ab51d23a16f3e3baf3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("PUBLISHED"));
    }

    @Test
    @Order(2)
    void testApproveDispatchTransactionAlreadyDispatched() {

        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/All_test_ready_to_publish_27af5261ab51d23a16f3e3baf3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("PUBLISHED"));

        given()
                .contentType("application/json")
                .body("{\n" +
                        "  \"organisationId\": \"dummy-organisation\",\n" +
                        "  \"transactionIds\": [\n" +
                        "    {\n" +
                        "      \"id\": \"All_test_published_27a89sd8d2f5261ab51d23a16f3e3baf3daa461b84ab4\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .when()
                .post("/api/transactions/publish")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("All_test_published_27a89sd8d2f5261ab51d23a16f3e3baf3daa461b84ab4"))
                .body("success[0]", equalTo(true))
        ;

        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/All_test_ready_to_publish_27af5261ab51d23a16f3e3baf3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("PUBLISHED"));

    }

    @Test
    @Order(3)
    void testApproveDispatchTransactionAlreadyMarkForDispatch() {

        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/All_test_ready_to_publish_27af5261ab51d23a16f3e3baf3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("PUBLISHED"));

        given()
                .contentType("application/json")
                .body("{\n" +
                        "  \"organisationId\": \"dummy-organisation\",\n" +
                        "  \"transactionIds\": [\n" +
                        "    {\n" +
                        "      \"id\": \"All_test_published_27a89sd8d2f5261ab51d23a16f3e3baf3daa461b84ab4\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .when()
                .post("/api/transactions/publish")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("All_test_published_27a89sd8d2f5261ab51d23a16f3e3baf3daa461b84ab4"))
                .body("success[0]", equalTo(true))
        ;
        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/All_test_ready_to_publish_27af5261ab51d23a16f3e3baf3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("PUBLISHED"));

    }

    @Test
    @Order(4)
    void testApproveDispatchTransactionNotReadyToPublish() {
        given()
                .contentType("application/json")
                .body("{\n" +
                        "  \"organisationId\": \"dummy-organisation\",\n" +
                        "  \"transactionIds\": [\n" +
                        "    {\n" +
                        "      \"id\": \"All_test_ready_to_approve_27af5261ab51d23a16f3e3baf3daa461b84ab4\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .when()
                .post("/api/transactions/publish")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("All_test_ready_to_approve_27af5261ab51d23a16f3e3baf3daa461b84ab4"))
                .body("success[0]", equalTo(false))
                .body("error[0].title", equalTo("TX_NOT_APPROVED"))

        ;
    }

    @Test
    @Order(5)
    void testApproveDispatchTransactionTransactionNotFound() {
        given()
                .contentType("application/json")
                .body("{\n" +
                        "  \"organisationId\": \"dummy-organisation\",\n" +
                        "  \"transactionIds\": [\n" +
                        "    {\n" +
                        "      \"id\": \"ReadyToApprove_1_8a283b41eab57add98278561ab51d23f3f3daa461b84aca\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .when()
                .post("/api/transactions/publish")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("ReadyToApprove_1_8a283b41eab57add98278561ab51d23f3f3daa461b84aca"))
                .body("success[0]", equalTo(false))
                .body("error[0].title", equalTo("TX_NOT_FOUND"))
        ;
    }

    @Test
    @Order(6)
    void testApproveDispatchTransactionFailedTransactionViolation() {
        given()
                .contentType("application/json")
                .body("{\n" +
                        "  \"organisationId\": \"dummy-organisation\",\n" +
                        "  \"transactionIds\": [\n" +
                        "    {\n" +
                        "      \"id\": \"Invalid_by_violation_27add98278561ab51d23a16f3e3baf3daa461b84ab4\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .when()
                .post("/api/transactions/publish")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("Invalid_by_violation_27add98278561ab51d23a16f3e3baf3daa461b84ab4"))
                .body("success[0]", equalTo(false))
                .body("error[0].title", equalTo("CANNOT_APPROVE_FAILED_TX"))
        ;
    }

    @Test
    @Order(7)
    void testApproveDispatchTransactionFailedTransactionItemRejected() {
        given()
                .contentType("application/json")
                .body("{\n" +
                        "  \"organisationId\": \"dummy-organisation\",\n" +
                        "  \"transactionIds\": [\n" +
                        "    {\n" +
                        "      \"id\": \"Invalid_by_rejection_27add98278561ab51d23a16f3e3baf3daa461b84ab4\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .when()
                .post("/api/transactions/publish")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("Invalid_by_rejection_27add98278561ab51d23a16f3e3baf3daa461b84ab4"))
                .body("success[0]", equalTo(false))
                .body("error[0].title", equalTo("CANNOT_APPROVE_REJECTED_TX"))
        ;
    }


}
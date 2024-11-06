package org.cardanofoundation.lob.app;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;


class AccountingCoreResourceTransactionApproveTest extends WebBaseIntegrationTest {
    @Test
    void testApproveTransaction() {
        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/ReadyToApprove_1_8a283b41eab57add98278561ab51d23f3f3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("APPROVE"));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "transactionIds": [
                            {
                              "id": "ReadyToApprove_1_8a283b41eab57add98278561ab51d23f3f3daa461b84ab4"
                            }
                          ]
                        }""")
                .when()
                .post("/api/transactions/approve")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("ReadyToApprove_1_8a283b41eab57add98278561ab51d23f3f3daa461b84ab4"))
                .body("success[0]", equalTo(true))
        ;
        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/ReadyToApprove_1_8a283b41eab57add98278561ab51d23f3f3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("PUBLISH"));
    }

    @Test
    void testApproveTransactionReadyToPublish() {
        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/All_test_ready_to_publish_27af5261ab51d23a16f3e3baf3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("PUBLISH"));

        given()
                .contentType("application/json")
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "transactionIds": [
                            {
                              "id": "All_test_ready_to_publish_27af5261ab51d23a16f3e3baf3daa461b84ab4"
                            }
                          ]
                        }""")
                .when()
                .post("/api/transactions/approve")
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
                .body("statistic", equalTo("PUBLISH"));
    }

    @Test
    void testApproveTransactionPublished() {
        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/All_test_published_27a89sd8d2f5261ab51d23a16f3e3baf3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("PUBLISH"));
        given()
                .contentType("application/json")
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "transactionIds": [
                            {
                              "id": "All_test_published_27a89sd8d2f5261ab51d23a16f3e3baf3daa461b84ab4"
                            }
                          ]
                        }""")
                .when()
                .post("/api/transactions/approve")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("All_test_published_27a89sd8d2f5261ab51d23a16f3e3baf3daa461b84ab4"))
                .body("success[0]", equalTo(true))
        ;
        given()
                .contentType("application/json")
                .when()
                .get("/api/transactions/All_test_published_27a89sd8d2f5261ab51d23a16f3e3baf3daa461b84ab4")
                .then()
                .statusCode(200)
                .body("statistic", equalTo("PUBLISH"));
    }

    @Test
    void testApproveTransactionTransactionNotFound() {
        given()
                .contentType("application/json")
                .body("{\n" +
                        "  \"organisationId\": \"75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94\",\n" +
                        "  \"transactionIds\": [\n" +
                        "    {\n" +
                        "      \"id\": \"ReadyToApprove_1_8a283b41eab57add98278561ab51d23f3f3daa461b84aca\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .when()
                .post("/api/transactions/approve")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("ReadyToApprove_1_8a283b41eab57add98278561ab51d23f3f3daa461b84aca"))
                .body("success[0]", equalTo(false))
                .body("error[0].title", equalTo("TX_NOT_FOUND"))
        ;
    }

    @Test
    void testApproveTransactionFailedTransactionViolation() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "transactionIds": [
                            {
                              "id": "Invalid_by_violation_27add98278561ab51d23a16f3e3baf3daa461b84ab4"
                            }
                          ]
                        }""")
                .when()
                .post("/api/transactions/approve")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("Invalid_by_violation_27add98278561ab51d23a16f3e3baf3daa461b84ab4"))
                .body("success[0]", equalTo(false))
                .body("error[0].title", equalTo("CANNOT_APPROVE_FAILED_TX"))
        ;

    }

    @Test
    void testApproveTransactionFailedTransactionItemRejected() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "transactionIds": [
                            {
                              "id": "Invalid_by_rejection_27add98278561ab51d23a16f3e3baf3daa461b84ab4"
                            }
                          ]
                        }""")
                .when()
                .post("/api/transactions/approve")
                .then()
                .statusCode(200)
                .body("id[0]", equalTo("Invalid_by_rejection_27add98278561ab51d23a16f3e3baf3daa461b84ab4"))
                .body("success[0]", equalTo(false))
                .body("error[0].title", equalTo("CANNOT_APPROVE_REJECTED_TX"));
    }

}

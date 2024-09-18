package org.cardanofoundation.lob.app;

import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;

@Slf4j
class AccountingCoreResourceBatchReprocessTest extends WebBaseIntegrationTest {

    @Test
    void testBatchReprocess() throws Exception {

        given()
                .contentType("application/json")
                .when()
                .get("/api/batches/TEST_All_state_b6x90wedd2d9e814b96436029a8ca22440f65c64ef236459e")
                .then()
                .statusCode(200)
                .body("batchStatistics.invalid", equalTo(4))
                .body("batchStatistics.pending", equalTo(2))
                .body("batchStatistics.approve", equalTo(1))
                .body("batchStatistics.publish", equalTo(2))
                .body("batchStatistics.published", equalTo(0))
                .body("batchStatistics.total", equalTo(9))
                .body("transactions.find { it.id == 'T_Vio_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461b84a' }.violations.code", containsInAnyOrder("DOCUMENT_MUST_BE_PRESENT", "EVENT_DATA_NOT_FOUND"))
                .body("transactions.find { it.items.find { item -> item.id == 'ITEM_Rej_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461b' } }.items.find { it.id == 'ITEM_Rej_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461b' }.rejectionReason", equalTo("INCORRECT_CURRENCY"))
                .body("transactions.find { it.items.find { item -> item.id == 'ITEM_Rej_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461a' } }.items.find { it.id == 'ITEM_Rej_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461a' }.rejectionReason", equalTo("REVIEW_PARENT_PROJECT_CODE"))

        ;

        given()
                .contentType("application/json")
                //.body(myJson)
                .when()
                .get("/api/batches/reprocess/TEST_All_state_b6x90wedd2d9e814b96436029a8ca22440f65c64ef236459e")
                .then()
                .statusCode(200)
                //.body("id", containsString(expectedUpdatedAt))
                .body("batchId", equalTo("TEST_All_state_b6x90wedd2d9e814b96436029a8ca22440f65c64ef236459e"))
        ;

        given()
                .contentType("application/json")
                .when()
                .get("/api/batches/TEST_All_state_b6x90wedd2d9e814b96436029a8ca22440f65c64ef236459e")
                .then()
                .statusCode(200)
                .body("id", equalTo("TEST_All_state_b6x90wedd2d9e814b96436029a8ca22440f65c64ef236459e"))
                .body("batchStatistics.invalid", equalTo(4))
                .body("batchStatistics.pending", equalTo(0))
                .body("batchStatistics.approve", equalTo(3))
                .body("batchStatistics.publish", equalTo(2))
                .body("batchStatistics.published", equalTo(0))
                .body("batchStatistics.total", equalTo(9))
                .body("transactions.find { it.items.find { item -> item.id == 'item_3e212e7639134ea08a39d1b7e3da27fe65ac0da6897f606c15dbaebfc1x' } }.items.find { it.id == 'item_3e212e7639134ea08a39d1b7e3da27fe65ac0da6897f606c15dbaebfc1x' }.rejectionReason", nullValue())
                .body("transactions.find { it.items.find { item -> item.id == 'item_3e212e7639134ea08a39d1b7e3da27fe65ac0da6897f606c15dbaebfc1z' } }.items.find { it.id == 'item_3e212e7639134ea08a39d1b7e3da27fe65ac0da6897f606c15dbaebfc1z' }.rejectionReason", equalTo("INCORRECT_CURRENCY"))
                .body("transactions.find { it.id == 'Pending_by_violation_27add98278561ab51d23a16f3e3baf3daa461b84ab4' }.violations.code", hasSize(0))
                .body("transactions.find { it.id == 'Pending_by_violation_27add98278561ab51d23a16f3e3baf3daa461b84ab4' }.statistic", equalTo("APPROVE"))
                .body("transactions.find { it.id == 'T_Vio_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461b84a' }.violations.code", contains("DOCUMENT_MUST_BE_PRESENT"))
                .body("transactions.find { it.id == 'T_Vio_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461b84a' }.statistic", equalTo("INVALID"))
                .body("transactions.find { it.items.find { item -> item.id == 'ITEM_Rej_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461b' } }.items.find { it.id == 'ITEM_Rej_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461b' }.rejectionReason", equalTo("INCORRECT_CURRENCY"))
                .body("transactions.find { it.items.find { item -> item.id == 'ITEM_Rej_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461a' } }.items.find { it.id == 'ITEM_Rej_inv_pen_9msd923md93mx923md93k1ab51d23a16f3e3baf3daa461a' }.rejectionReason", nullValue())
        ;

    }


}
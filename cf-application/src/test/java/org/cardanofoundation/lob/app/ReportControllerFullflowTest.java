package org.cardanofoundation.lob.app;

import io.restassured.http.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

@Execution(ExecutionMode.SAME_THREAD)
class ReportControllerFullflowTest extends WebBaseIntegrationTest {
    @MockBean
    private Clock clock;

    private static final String ORG_ID = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94";

    @BeforeEach
    void setup() {
        Instant fixedInstant = Instant.parse("2025-02-06T12:00:00Z");
        ZoneId zoneId = ZoneId.of("UTC");
        Mockito.when(clock.instant()).thenReturn(fixedInstant);
        Mockito.when(clock.getZone()).thenReturn(zoneId);
    }

    @Test
    @Order(1)
    void reportListEmptyReports() {
        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/report-list/%s".formatted(ORG_ID))
                .then()
                .statusCode(200)
                //.body("id", containsString(expectedUpdatedAt))
                .body("success", equalTo(true))
                .body("report", equalTo(new ArrayList<>()));
    }

    @Test
    @Order(2)
    void createReport() {
        String inputBalanceSheetCreate = """
                {
                    "organisationID": "%s",
                    "reportType": "BALANCE_SHEET",
                    "intervalType": "MONTH",
                    "year": 2023,
                    "period": 1,
                      "cashAndCashEquivalents": "1",
                      "cryptoAssets": "2",
                      "otherReceivables": "3",
                      "prepaymentsAndOtherShortTermAssets": "4",
                      "financialAssets": "5",
                      "investments": "6",
                      "propertyPlantEquipment": "7",
                      "intangibleAssets": "8",
                
                      "tradeAccountsPayables": "1",
                      "otherCurrentLiabilities": "2",
                      "accrualsAndShortTermProvisions": "3",
                      "provisions": "4",
                      "capital": "5",
                      "resultsCarriedForward": "6",
                      "profitForTheYear": "15"
                    }
                """.formatted(ORG_ID);

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputBalanceSheetCreate)
                .when()
                .post("/api/report-create")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].error", equalTo(null))
                .body("report[0].reportId", equalTo("8fb79106c39a8e1f227e5cb1931a5ad1898dd5e06b6d0fb5d8ac21941f3bf3dd"))
                .body("report[0].publish", equalTo(false))
                .body("report[0].ver", equalTo(0))
                .body("report[0].canBePublish", equalTo(true))
                ;
    }

    @Test
    @Order(3)
    void publishReport() {
        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body("""
                        {
                          "organisationId": "%s",
                          "reportId": "8fb79106c39a8e1f227e5cb1931a5ad1898dd5e06b6d0fb5d8ac21941f3bf3dd"
                        }""".formatted(ORG_ID))

                .when()
                .post("/api/report-publish")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].error", equalTo(null))
                .body("report[0].reportId", equalTo("8fb79106c39a8e1f227e5cb1931a5ad1898dd5e06b6d0fb5d8ac21941f3bf3dd"))
                .body("report[0].publish", equalTo(true))
                .body("report[0].ver", equalTo(0))
                .body("report[0].canBePublish", equalTo(true))

        ;
        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/report-list/%s".formatted(ORG_ID))
                .then()
                .statusCode(200)
                //.body("id", containsString(expectedUpdatedAt))
                .body("success", equalTo(true))
                .body("total", equalTo(1))
                .body("report[0].error", equalTo(null))
                .body("report[0].reportId", equalTo("8fb79106c39a8e1f227e5cb1931a5ad1898dd5e06b6d0fb5d8ac21941f3bf3dd"))
                .body("report[0].publish", equalTo(true))
                .body("report[0].canBePublish", equalTo(true))
        ;
    }
}

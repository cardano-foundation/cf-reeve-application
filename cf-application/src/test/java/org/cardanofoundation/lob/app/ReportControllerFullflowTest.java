package org.cardanofoundation.lob.app;

import io.restassured.http.Header;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import lombok.val;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            .get("/api/report-list/75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
            .then()
            .statusCode(200)
            //.body("id", containsString(expectedUpdatedAt))
            .body("success", equalTo(true))
            .body("report", equalTo(new ArrayList<>()));
    }

    @Test
    @Order(2)
    void createReport() {
        val inputBalanceSheetCreate = """
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
                      "intangibleAssets": "113",
                
                      "tradeAccountsPayables": "1",
                      "otherCurrentLiabilities": "2",
                      "accrualsAndShortTermProvisions": "3",
                      "provisions": "4",
                      "capital": "5",
                      "resultsCarriedForward": "6",
                      "profitForTheYear": "120"
                    }""".formatted(ORG_ID);

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputBalanceSheetCreate)
                .when()
                .post("/api/report-create")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("8fb79106c39a8e1f227e5cb1931a5ad1898dd5e06b6d0fb5d8ac21941f3bf3dd"))
                .body("report[0].publish", equalTo(false))
                .body("report[0].ver", equalTo(0))
                .body("report[0].canBePublish", equalTo(true))
                .body("report[0].error", equalTo(null))
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
                .body("report[0].reportId", equalTo("8fb79106c39a8e1f227e5cb1931a5ad1898dd5e06b6d0fb5d8ac21941f3bf3dd"))
                .body("report[0].publish", equalTo(true))
                .body("report[0].ver", equalTo(0))
                .body("report[0].canBePublish", equalTo(true))
                .body("report[0].error", equalTo(null))
         ;
    }

    @Test
    @Order(4)
    void listReport() {
                given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/report-list/75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
                .then()
                .statusCode(200)
                //.body("id", containsString(expectedUpdatedAt))
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("1e1da8241a6e0349a31f7cbadc057e2c499964025b653f77bb5b5da4f7a9c55d"))
                .body("report[0].publish", equalTo(true))
                .body("report[0].canBePublish", equalTo(true))
                .body("report[0].error", equalTo(null))
                .body("report[1].reportId", equalTo("8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"))
                .body("report[1].publish", equalTo(false))
                .body("report[1].canBePublish", equalTo(true))
                .body("report[1].error", equalTo(null));
    }

    @Test
    @Order(5)
    void createSecondReport() {
        val inputIncomeStatementUpdate = """
                {
                   "organisationID": "%s",
                   "reportType": "INCOME_STATEMENT",
                   "intervalType": "MONTH",
                   "year": 2023,
                   "period": 1,
                   "otherIncome": "1",
                   "buildOfLongTermProvision": "2",
                   "costOfProvidingServices": "3",
                   "personnelExpenses": "4",
                   "rentExpenses": "5",
                   "generalAndAdministrativeExpenses": "6",
                   "depreciationAndImpairmentLossesOnTangibleAssets": "7",
                   "amortizationOnIntangibleAssets": "8",
                   "financialRevenues": "9",
                   "realisedGainsOnSaleOfCryptocurrencies": "10",
                   "stakingRewardsIncome": "11",
                   "netIncomeOptionsSale": "12",
                   "financialExpenses": "13",
                   "extraordinaryExpenses": "14",
                   "incomeTaxExpense": "16"
                   }""".formatted(ORG_ID);

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputIncomeStatementUpdate)
                .when()
                .post("/api/report-create")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("2e052f113132803ed48036fe4e1621dbeb33e0cfa193a3f546b0e340f05e8a1a"))
                .body("report[0].publish", equalTo(false))
                .body("report[0].canBePublish", equalTo(false))
                .body("report[0].error.title", equalTo("PROFIT_FOR_THE_YEAR_MISMATCH"))
        ;
    }

    @Test
    @Disabled
    void reportBothReportsTest() {
        Instant fixedInstant = Instant.parse("2025-02-06T12:00:00Z"); // Set a fixed test time
        ZoneId zoneId = ZoneId.of("UTC");
        Mockito.when(clock.instant()).thenReturn(fixedInstant);
        Mockito.when(clock.getZone()).thenReturn(zoneId);

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "reportId": "2e052f113132803ed48036fe4e1621dbeb33e0cfa193a3f546b0e340f05e8a1a"
                        }""")
                .when()
                .post("/api/report-publish")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("error.title", equalTo("PROFIT_FOR_THE_YEAR_MISMATCH"))
        ;

        val inputIncomeStatementCorrection = """
                {
                   "organisationID": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                   "reportType": "INCOME_STATEMENT",
                   "intervalType": "MONTH",
                   "year": 2023,
                   "period": 1,
                   "otherIncome": "1",
                   "buildOfLongTermProvision": "2",
                   "costOfProvidingServices": "3",
                   "personnelExpenses": "4",
                   "rentExpenses": "5",
                   "generalAndAdministrativeExpenses": "6",
                   "depreciationAndImpairmentLossesOnTangibleAssets": "7",
                   "amortizationOnIntangibleAssets": "8",
                   "financialRevenues": "9",
                   "realisedGainsOnSaleOfCryptocurrencies": "10",
                   "stakingRewardsIncome": "11",
                   "netIncomeOptionsSale": "12",
                   "financialExpenses": "13",
                   "extraordinaryExpenses": "14",
                   "incomeTaxExpense": "15"
                   }""";

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputIncomeStatementCorrection)
                .when()
                .post("/api/report-create")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("2e052f113132803ed48036fe4e1621dbeb33e0cfa193a3f546b0e340f05e8a1a"))
                .body("report[0].publish", equalTo(false))
        ;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "reportId": "2e052f113132803ed48036fe4e1621dbeb33e0cfa193a3f546b0e340f05e8a1a"
                        }""")
                .when()
                .post("/api/report-publish")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("2e052f113132803ed48036fe4e1621dbeb33e0cfa193a3f546b0e340f05e8a1a"))
                .body("report[0].publish", equalTo(true))
        ;

        ExtractableResponse<Response> success = given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/report-list/75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract();
        String reportIdZero = success.body().path("report[0].reportId");
        String reportIdOne = success.body().path("report[1].reportId");
        Set<String> reportIds = Set.of(reportIdZero, reportIdOne);
        assertTrue(reportIds.contains("8fb79106c39a8e1f227e5cb1931a5ad1898dd5e06b6d0fb5d8ac21941f3bf3dd"));
        assertTrue(reportIds.contains("2e052f113132803ed48036fe4e1621dbeb33e0cfa193a3f546b0e340f05e8a1a"));
    }

}

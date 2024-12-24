package org.cardanofoundation.lob.app;

import io.restassured.http.Header;
import lombok.val;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

class ReportControllerFullflowTest extends WebBaseIntegrationTest {

    @Test
    void reportBothReportsTest() {

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


        val inputBalanceSheetCreate = """
                {
                        "organisationID": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
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
                    }""";

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputBalanceSheetCreate)
                .when()
                .post("/api/report-create")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"))
                .body("report[0].publish", equalTo(false));


        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "reportId": "8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"
                        }""")
                .when()
                .post("/api/report-publish")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("error.title", equalTo("NO_RELATED_REPORT"))
        ;


        val inputIncomeStatementCreate = """
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
                .body(inputIncomeStatementCreate)
                .when()
                .post("/api/report-create")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("1e1da8241a6e0349a31f7cbadc057e2c499964025b653f77bb5b5da4f7a9c55d"))
                .body("report[0].publish", equalTo(false));


        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/report-list/75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
                .then()
                .statusCode(200)
                //.body("id", containsString(expectedUpdatedAt))
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"))
                .body("report[0].publish", equalTo(false))
                .body("report[1].reportId", equalTo("1e1da8241a6e0349a31f7cbadc057e2c499964025b653f77bb5b5da4f7a9c55d"))
                .body("report[1].publish", equalTo(false));


        val inputIncomeStatementUpdate = """
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
                   "incomeTaxExpense": "16"
                   }""";

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body(inputIncomeStatementUpdate)
                .when()
                .post("/api/report-create")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("error.title", equalTo("PROFIT_FOR_THE_YEAR_MISMATCH"));

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "reportId": "8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"
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
                .body("report[0].reportId", equalTo("1e1da8241a6e0349a31f7cbadc057e2c499964025b653f77bb5b5da4f7a9c55d"))
                .body("report[0].publish", equalTo(false))
        ;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "reportId": "8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"
                        }""")
                .when()
                .post("/api/report-publish")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"))
                .body("report[0].publish", equalTo(true))
        ;

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/report-list/75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"))
                .body("report[0].publish", equalTo(true))
                .body("report[1].reportId", equalTo("1e1da8241a6e0349a31f7cbadc057e2c499964025b653f77bb5b5da4f7a9c55d"))
                .body("report[1].publish", equalTo(false));

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .body("""
                        {
                          "organisationId": "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94",
                          "reportId": "8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"
                        }""")
                .when()
                .post("/api/report-publish")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"))
                .body("report[0].publish", equalTo(true));

        given()
                .contentType("application/json")
                .header(new Header("Accept-Language", "en-US"))
                .when()
                .get("/api/report-list/75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("report[0].reportId", equalTo("1e1da8241a6e0349a31f7cbadc057e2c499964025b653f77bb5b5da4f7a9c55d"))
                .body("report[0].publish", equalTo(true))
                .body("report[1].reportId", equalTo("8d8209cb555b7c71a5a90ad52ce49f4ea4bd1948489a49cd5eedc3fab958d968"))
                .body("report[1].publish", equalTo(true));

    }

}

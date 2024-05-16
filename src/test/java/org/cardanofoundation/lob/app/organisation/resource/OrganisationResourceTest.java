package org.cardanofoundation.lob.app.organisation.resource;

import org.cardanofoundation.lob.app.WebBaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class OrganisationResourceTest extends WebBaseIntegrationTest {

    @Test
    void testOrganisationList() {
        LocalDate today = LocalDate.now();
        LocalDate monthsAgo = today.minusMonths(36);
        LocalDate yesterday = today.minusDays(1);

        given()
                .when()
                .get("/api/organisation")
                .then()
                .statusCode(200)
                .body("name[0]",equalTo("Cardano Foundation"))
                .body("id[0]", equalTo("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
                .body("description[0]", equalTo("CHE-184477354"))
                .body("currencyId[0]", equalTo("ISO_4217:CHF"))
                .body("accountPeriodFrom[0]", equalTo(monthsAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .body("accountPeriodTo[0]", equalTo(yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
        ;
    }

    @Test
    void testOrganisationDetailSpecific() {
        given()
                .when()
                .get("/api/organisation/75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
                .then()
                .statusCode(200)
                .body("id",equalTo("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
                .body("name",equalTo("Cardano Foundation"))
        ;
    }

    @Test
    void testOrganisationDetailSpecificNotFound() {
        given()
                .when()
                .get("/api/organisation/85f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
                .then()
                .statusCode(404)
                .body("title",equalTo("ORGANISATION_NOT_FOUND"))
                .body("detail",equalTo("Unable to find Organisation by Id: 85f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94"))
        ;
    }

}
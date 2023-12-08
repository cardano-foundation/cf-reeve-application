package org.cardanofoundation.lob.app;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class HelloWebIntegrationTest extends WebBaseIntegrationTest {

    @Test
    public void hello() {
        given()
                .when()
                .get("/api/netsuite/hello")
                .then()
                .statusCode(200);
    }

    @Test
    public void findNetSuiteIngestion() {
        given()
                .when()
                .get("/api/netsuite/ingestion/86d612cb0782a3b001f7374430211c83")
                .then()
                .statusCode(200);
    }

}

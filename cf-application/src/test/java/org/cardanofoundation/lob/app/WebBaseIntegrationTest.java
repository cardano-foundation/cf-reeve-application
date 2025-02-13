package org.cardanofoundation.lob.app;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ComponentScan
@EnableJpaRepositories
@EntityScan
@TestInstance(PER_CLASS)
@SpringJUnitConfig(classes = { LobServiceApp.class, LobServiceApplicationTest.class } )
@Slf4j
@ActiveProfiles("test")
public class WebBaseIntegrationTest {

    @LocalServerPort
    protected int serverPort;

    protected WireMockServer wireMockServer;

    protected int randomWebMockPort = 19000;

    @BeforeAll
    public void setUp() {
        log.info("WireMockServer port: {}", randomWebMockPort);
        log.info("Local server port: {}", serverPort);

        wireMockServer = new WireMockServer(randomWebMockPort);
        wireMockServer.start();

        RestAssured.port = serverPort;
        RestAssured.baseURI = "http://localhost";
    }

//    @BeforeEach
//    void resetWireMock() {
//        wireMockServer.resetAll();
//    }

    @BeforeAll
    public void clearDatabase(@Autowired Flyway flyway){
        flyway.clean();
        flyway.migrate();
    }

    @AfterAll
    public void tearDown() {
        wireMockServer.stop();
    }

}

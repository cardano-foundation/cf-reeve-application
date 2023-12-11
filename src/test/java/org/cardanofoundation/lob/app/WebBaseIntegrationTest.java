package org.cardanofoundation.lob.app;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan
@EnableJpaRepositories
@EntityScan
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({ "test" })
@SpringJUnitConfig(classes = { LobServiceApp.class, LobServiceApplicationTest.class } )
@Slf4j
public class WebBaseIntegrationTest {

    @LocalServerPort
    protected int serverPort;

    protected WireMockServer wireMockServer;

    protected int randomWebMockPort = 1024 + (int) (Math.random() * 1000);

    @BeforeAll
    public void setUp() {
        log.info("WireMockServer port: {}", randomWebMockPort);
        log.info("Local server port: {}", serverPort);

        wireMockServer = new WireMockServer(randomWebMockPort);
        wireMockServer.start();

        RestAssured.port = serverPort;
        RestAssured.baseURI = "http://localhost";
    }

    @AfterAll
    public void tearDown() {
        wireMockServer.stop();
    }

}

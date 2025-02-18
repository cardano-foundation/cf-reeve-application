package org.cardanofoundation.lob.app;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@TestConfiguration(proxyBeanMethods = false)
public class LobServiceApplicationTest {
    private static final String POSTGRES_IMAGE = "postgres:16.3";

//    @Bean
//    @ServiceConnection
//    public PostgreSQLContainer<?> postgreSQLContainer() {
//        return new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE));
//    }

//    @Bean
//    @ServiceConnection
//    public RabbitMQContainer rabbitMQContainer() {
//        return new RabbitMQContainer(DockerImageName.parse(RABBIT_IMAGE));
//    }

}

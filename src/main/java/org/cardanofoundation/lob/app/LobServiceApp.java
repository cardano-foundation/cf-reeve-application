package org.cardanofoundation.lob.app;

import io.micrometer.core.aop.TimedAspect;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.cardanofoundation.lob.app.netsuite.NetsuiteService;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.springframework.aot.hint.ExecutableMode.INVOKE;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, ErrorMvcAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class })
@EnableJpaRepositories( { "org.cardanofoundation.lob", "org.springframework.modulith.events.jpa" } )
@EntityScan(basePackages = { "org.cardanofoundation.lob", "org.springframework.modulith.events.jpa" } )
@ComponentScan(basePackages = {
        "org.cardanofoundation.lob.app"
})
@EnableTransactionManagement
@EnableAsync
@ImportRuntimeHints(LobServiceApp.Hints.class)
@Slf4j
@RequiredArgsConstructor
public class LobServiceApp {

    private final NetsuiteService netsuiteService;

    public static void main(String[] args) {
        SpringApplication.run(LobServiceApp.class, args);
    }

    @Bean
    public CommandLineRunner onStart() {
        return (args) -> {
            log.info("Starting Lob Service...");

            netsuiteService.scheduleNetsuiteIngestionEvent();

            log.info("Lob Service started.");
        };
    }

    static class Hints implements RuntimeHintsRegistrar {

        @Override
        @SneakyThrows
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerMethod(TimedAspect.class.getMethod("timedMethod", ProceedingJoinPoint.class), INVOKE);
        }
    }

}

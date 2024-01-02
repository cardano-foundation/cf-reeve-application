package org.cardanofoundation.lob.app;

import io.micrometer.core.aop.TimedAspect;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.cardanofoundation.lob.module.netsuite.domain.ScheduledIngestionEvent;
import org.cardanofoundation.lob.module.netsuite.service.NetsuiteService;
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
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;
import java.time.Duration;

import static org.springframework.aot.hint.ExecutableMode.INVOKE;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, ErrorMvcAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class })
@EnableJpaRepositories( { "org.cardanofoundation.lob", "org.springframework.modulith.events.jpa" } )
@EntityScan(basePackages = { "org.cardanofoundation.lob", "org.springframework.modulith.events.jpa" } )
@ComponentScan(basePackages = {
        "org.cardanofoundation.lob.app.service",
        "org.cardanofoundation.lob.app.config",
        "org.cardanofoundation.lob.app.resource",
        "org.cardanofoundation.lob.module.netsuite.config",

        "org.cardanofoundation.lob.module.core.config",
        "org.cardanofoundation.lob.module.core.domain",
        "org.cardanofoundation.lob.module.core.service",
})
@EnableTransactionManagement
@EnableAsync
@ImportRuntimeHints(LobServiceApp.Hints.class)
@Slf4j
@RequiredArgsConstructor
public class LobServiceApp {

    private final ScheduledTaskRegistrar scheduledTaskRegistrar;

    private final NetsuiteService netsuiteService;


    public static void main(String[] args) {
        SpringApplication.run(LobServiceApp.class, args);
    }

    @Bean
    public CommandLineRunner onStart() {
        return (args) -> {
            log.info("Starting Lob Service...");

            scheduledTaskRegistrar.scheduleFixedRateTask(new FixedRateTask(() -> {
                log.info("Schedule Netsuite ingestion job...");

                try {
                    netsuiteService.process(new ScheduledIngestionEvent("system"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, Duration.ofSeconds(10), Duration.ofSeconds(1)));

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

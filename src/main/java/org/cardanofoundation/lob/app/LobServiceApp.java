package org.cardanofoundation.lob.app;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.modulith.Modulith;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.time.Clock;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, ErrorMvcAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class })
@EnableJpaRepositories( { "org.cardanofoundation.lob", "org.springframework.modulith.events.jpa" } )
@EntityScan(basePackages = { "org.cardanofoundation.lob.app.support.web_support.internal",
                             "org.cardanofoundation.lob.app.support.audit_support.internal",
                             "org.cardanofoundation.lob",
                             "org.springframework.modulith.events.jpa"
                           } )
@ComponentScan(basePackages = {
        "org.cardanofoundation.lob.app"
})
@Modulith
@EnableTransactionManagement
@EnableAsync
//@ImportRuntimeHints(LobServiceApp.Hints.class)
@Slf4j
@RequiredArgsConstructor
public class LobServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(LobServiceApp.class, args);
    }

    @Bean
    public CommandLineRunner onStart() {
        return (args) -> {
            log.info("Starting Lob Service...");

            log.info("Lob Service started.");
        };
    }

    @Configuration
    @EnableCaching
    public class CacheConfig {

        // https://asbnotebook.com/etags-in-restful-services-spring-boot/
        @Bean
        public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
            log.info("Registering ShallowEtagHeaderFilter...");
            return new ShallowEtagHeaderFilter();
        }

    }

    @Configuration
    public class MetricsConfig {

        @Bean
        public TimedAspect timedAspect(MeterRegistry registry) {
            log.info("Registering TimedAspect...");
            return new TimedAspect(registry);
        }

    }

    @Configuration
    @EnableScheduling
    public class SchedulerConfig {

        @Bean
        public ScheduledTaskRegistrar scheduledTaskRegistrar() {
            log.info("Registering ScheduledTaskRegistrar...");
            ScheduledTaskRegistrar scheduledTaskRegistrar = new ScheduledTaskRegistrar();
            scheduledTaskRegistrar.setScheduler(threadPoolTaskScheduler());

            return scheduledTaskRegistrar;
        }

        @Bean
        TaskScheduler threadPoolTaskScheduler() {
            log.info("Registering TaskScheduler...");
            val scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(20);
            scheduler.setThreadNamePrefix("job-");
            scheduler.setAwaitTerminationSeconds(60);

            return scheduler;
        }

    }

    @Configuration
    public class TimeConfig {

        @Bean
        public Clock clock() {
            log.info("Registering Clock...");
            return Clock.systemDefaultZone();
        }

    }

}

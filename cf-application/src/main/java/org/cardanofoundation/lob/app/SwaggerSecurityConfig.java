package org.cardanofoundation.lob.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration(proxyBeanMethods = false)
public class SwaggerSecurityConfig {

    @Bean
    WebSecurityCustomizer swaggerWebSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v3/api-docs/**");
    }
}

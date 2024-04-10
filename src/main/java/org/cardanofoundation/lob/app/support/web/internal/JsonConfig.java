package org.cardanofoundation.lob.app.support.web.internal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;

@Configuration
public class JsonConfig {

    @Bean
    @Primary
    public ObjectMapper jsonMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(NON_NULL)
                .enable(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .registerModules(new JavaTimeModule(), new Jdk8Module());
    }

}

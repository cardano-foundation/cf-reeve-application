package org.cardanofoundation.lob.app.netsuite.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.val;
import org.cardanofoundation.lob.app.netsuite.util.NetSuiteDateDeserialiser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;

@Configuration
public class JsonConfig {

    @Bean("netSuiteJsonMapper")
    @Qualifier("netSuiteJsonMapper")
    public ObjectMapper jsonMapper() {
        val javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(java.time.LocalDateTime.class, new NetSuiteDateDeserialiser());

        return new ObjectMapper()
                .setSerializationInclusion(NON_NULL)
                .enable(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .registerModule(new JavaTimeModule());
    }

}

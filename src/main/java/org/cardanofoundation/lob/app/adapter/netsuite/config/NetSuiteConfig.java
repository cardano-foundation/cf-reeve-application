package org.cardanofoundation.lob.app.adapter.netsuite.config;

import org.cardanofoundation.lob.app.adapter.netsuite.client.NetSuite10Api;
import org.scribe.builder.ServiceBuilder;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.scribe.model.SignatureType.Header;

@Configuration
@ComponentScan(basePackages = {
        "org.cardanofoundation.lob.module.netsuite.resource",
        "org.cardanofoundation.lob.module.netsuite.config",
        "org.cardanofoundation.lob.module.netsuite.service"
})
public class NetSuiteConfig {

    @Bean
    public OAuthService netsuiteOAuthService(
            @Value("${lob.netsuite.client.consumer_key}") String consumerKey,
            @Value("${lob.netsuite.client.consumer_secret}") String consumerSecret
) {
        return new ServiceBuilder()
                .apiKey(consumerKey)
                .apiSecret(consumerSecret)
                .signatureType(Header)
                .provider(NetSuite10Api.class)
                .build();
    }

}

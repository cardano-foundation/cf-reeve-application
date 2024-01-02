package org.cardanofoundation.lob.module.netsuite.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "org.cardanofoundation.lob.module.netsuite.resource",
        "org.cardanofoundation.lob.module.netsuite.config",
        "org.cardanofoundation.lob.module.netsuite.service"
})
public class NetSuiteConfig {
}

dependencies {
    implementation("org.zalando:problem-spring-web-starter:0.29.1")
    implementation("io.vavr:vavr:0.10.4")

    implementation("org.cardanofoundation:cf-lob-platform-accounting_reporting_core:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-netsuite_altavia_erp_adapter:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-organisation:${property("cfLobPlatformVersion")}")
}

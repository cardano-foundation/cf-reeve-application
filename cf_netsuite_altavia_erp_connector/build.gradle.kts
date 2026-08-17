dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.vavr:vavr:0.10.4")

    implementation("org.cardanofoundation:cf-lob-platform-accounting_reporting_core:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-netsuite_altavia_erp_adapter:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-organisation:${property("cfLobPlatformVersion")}")
}

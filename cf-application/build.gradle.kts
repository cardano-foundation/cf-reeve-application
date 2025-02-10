plugins {
    id("org.springframework.boot") version "3.3.0"
}
val isKafkaEnabled: Boolean = System.getenv("KAFKA_ENABLED")?.toBooleanStrictOrNull() ?: true
dependencies {
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    //implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-starter-jdbc")
    // Kafka
    if(isKafkaEnabled) {
        runtimeOnly("org.springframework.modulith:spring-modulith-events-kafka")
        implementation("org.springframework.kafka:spring-kafka")
    }
    // RabbitMQ
//    implementation("org.springframework.modulith:spring-modulith-events-amqp")
//    implementation("org.springframework.boot:spring-boot-starter-amqp")

    implementation("org.jmolecules.integrations:jmolecules-starter-ddd")

    implementation(project(":cf_netsuite_altavia_erp_connector"))
    implementation("org.cardanofoundation:cf-lob-platform-organisation:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-support:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-notification_gateway:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-netsuite_altavia_erp_adapter:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-blockchain_publisher:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-accounting_reporting_core:${property("cfLobPlatformVersion")}")
}

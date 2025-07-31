plugins {
    id("org.springframework.boot") version "3.3.0"
    id("org.graalvm.buildtools.native") version "0.11.0"
}
val isKafkaEnabled: Boolean = System.getenv("KAFKA_ENABLED")?.toBooleanStrictOrNull() ?: true
dependencies {
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    //implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Kafka
    if(isKafkaEnabled) {
        implementation("org.springframework.kafka:spring-kafka")
    }
    // RabbitMQ
//    implementation("org.springframework.boot:spring-boot-starter-amqp")

    implementation(project(":cf_netsuite_altavia_erp_connector"))
    implementation("org.cardanofoundation:cf-lob-platform-organisation:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-support:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-notification_gateway:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-netsuite_altavia_erp_adapter:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-csv_erp_adapter:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-blockchain_publisher:${property("cfLobPlatformVersion")}")
    implementation("org.cardanofoundation:cf-lob-platform-accounting_reporting_core:${property("cfLobPlatformVersion")}") {
        exclude(group = "io.hypersistence", module = "hypersistence-utils-hibernate-63")
    }
}

tasks.withType<org.springframework.boot.gradle.tasks.aot.ProcessAot> {
    args("--spring.profiles.active=dev--yaci-dev-kit,pre-prod-prod")
}

graalvmNative {
    binaries {
        named("main") {
            // mainClass.set("org.cardanofoundation.lob.app.LobServiceApp")
            sharedLibrary.set(false)
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
            })
        }
    }
}


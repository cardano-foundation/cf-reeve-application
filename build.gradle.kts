import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.PitestTask

plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("info.solidsoft.pitest") version "1.15.0"
}

group = "de.cardanofoundation"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
       extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2023.0.0"
extra["springModulithVersion"] = "1.1.3"
extra["jMoleculesVersion"] = "2023.1.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    //implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.json:json:20211205")

    implementation("org.springframework.data:spring-data-envers")

    implementation("org.flywaydb:flyway-core")

    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-starter-jdbc")
    implementation("org.springframework.modulith:spring-modulith-events-amqp")

    implementation("org.jmolecules.integrations:jmolecules-starter-ddd")

    // needed to store json via JPA in PostgreSQL
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.7.4")

    implementation("org.springframework.boot:spring-boot-starter-amqp")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.postgresql:postgresql")

    runtimeOnly("org.springframework.modulith:spring-modulith-actuator")


//  runtimeOnly("org.springframework.modulith:spring-modulith-observability")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    testImplementation("org.springframework.modulith:spring-modulith-starter-test")

    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:rabbitmq")

    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")
    implementation("org.zalando:problem-spring-web-starter:0.29.1")
    implementation("io.vavr:vavr:0.10.4")
    implementation("me.paulschwarz:spring-dotenv:4.0.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    //implementation("org.javers:javers-spring-boot-starter-sql:7.3.7")
    //implementation("org.javers:javers-spring:7.3.7")

    //implementation("com.github.scribejava:scribejava-core:8.3.3")
    implementation("org.scribe:scribe:1.3.7") // needed for OAuth 1.0 for NetSuite Module

    implementation("javax.xml.bind", "jaxb-api", "2.3.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.2") // needed for OAuth 1.0 for NetSuite Module

    implementation("com.networknt:json-schema-validator:1.4.0")

    implementation("com.bloxbean.cardano:cardano-client-crypto:0.5.1")
    implementation("com.bloxbean.cardano:cardano-client-backend-blockfrost:0.5.1")
    implementation("com.bloxbean.cardano:cardano-client-quicktx:0.5.1")

    implementation("com.google.guava:guava:33.1.0-jre")

    implementation("org.apache.commons:commons-collections4:4.4")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testImplementation("org.wiremock:wiremock-standalone:3.3.1")
    testImplementation("net.jqwik:jqwik:1.8.4") // Jqwik for property-based testing
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.pitest:pitest-junit5-plugin:1.2.1")

}

dependencyManagement {
    imports {
       mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
       mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
       mavenBom("org.jmolecules:jmolecules-bom:${property("jMoleculesVersion")}")
    }
}

tasks {
    val ENABLE_PREVIEW = "--enable-preview"

    withType<JavaCompile>() {
        options.compilerArgs.add(ENABLE_PREVIEW)
        //options.compilerArgs.add("-Xlint:preview")
    }

    withType<Test>() {
        useJUnitPlatform()
        jvmArgs(ENABLE_PREVIEW)
    }

    withType<PitestTask>() {
        jvmArgs(ENABLE_PREVIEW)
    }
    withType<JavaExec>() {
        jvmArgs(ENABLE_PREVIEW)
    }
}
pitest {
    //adds dependency to org.pitest:pitest-junit5-plugin and sets "testPlugin" to "junit5"
    jvmArgs.add("--enable-preview")
    targetClasses.set(setOf("org.cardanofoundation.lob.app.*"))
    targetTests.set(setOf("org.cardanofoundation.lob.app.*"))
    exportLineCoverage = true
    timestampedReports = false
    threads = 2
    // ...
}

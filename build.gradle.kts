import info.solidsoft.gradle.pitest.PitestTask
import org.gradle.api.JavaVersion.VERSION_21

plugins {
    java
    id("io.spring.dependency-management") version "1.1.5"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("info.solidsoft.pitest") version "1.15.0"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "info.solidsoft.pitest")

    group = "de.cardanofoundation"
    version = "0.0.1-SNAPSHOT"

    sourceSets {
        named("main") {
            java {
                setSrcDirs(listOf("src/main/java"))
            }
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }

    java {
        sourceCompatibility = VERSION_21
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    extra["springBootVersion"] = "3.3.3"
    extra["springCloudVersion"] = "2023.0.0"
    extra["jMoleculesVersion"] = "2023.1.0"
    extra["cfLobPlatformVersion"] = "1.0.0"

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.32")
        annotationProcessor("org.projectlombok:lombok:1.18.32")

        implementation("org.javers:javers-core:7.6.1")
        implementation("org.apache.httpcomponents.client5:httpclient5:5.3")

        testCompileOnly("org.projectlombok:lombok:1.18.32")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
        testImplementation("io.rest-assured:rest-assured:5.5.0")
        testImplementation("org.wiremock:wiremock-standalone:3.9.1")
        testImplementation("net.jqwik:jqwik:1.9.0") // Jqwik for property-based testing
        testImplementation("org.assertj:assertj-core:3.26.0")
        testImplementation("org.pitest:pitest-junit5-plugin:1.2.1")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.boot:spring-boot-testcontainers")

        testImplementation("org.testcontainers:junit-jupiter")
        testImplementation("org.testcontainers:postgresql")
        testImplementation("org.scribe:scribe:1.3.7") // needed for OAuth 1.0 for NetSuite Module
        testImplementation("org.flywaydb:flyway-core")
        testImplementation("org.flywaydb:flyway-database-postgresql")
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
            mavenBom("org.jmolecules:jmolecules-bom:${property("jMoleculesVersion")}")
        }
    }

    tasks {
        val ENABLE_PREVIEW = "--enable-preview"

        withType<JavaCompile> {
            options.compilerArgs.add(ENABLE_PREVIEW)
            //options.compilerArgs.add("-Xlint:preview")
            val isKafkaEnabled: Boolean = System.getenv("KAFKA_ENABLED")?.toBooleanStrictOrNull() ?: true
            if (!isKafkaEnabled) {
                exclude("**/kafka/**")
            }

        }

        withType<Test> {
            useJUnitPlatform()
            jvmArgs(ENABLE_PREVIEW)
        }

        withType<PitestTask> {
            jvmArgs(ENABLE_PREVIEW)
        }

        withType<JavaExec> {
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
    }

}

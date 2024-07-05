plugins {
    id("org.springframework.boot") version "3.3.0"
}

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
    implementation("org.springframework.modulith:spring-modulith-events-amqp")

    implementation("org.springframework.boot:spring-boot-starter-amqp")

    implementation("org.jmolecules.integrations:jmolecules-starter-ddd")

    implementation(project(":accounting_reporting_core"))
    implementation(project(":organisation"))
    implementation(project(":support"))
    implementation(project(":blockchain_publisher"))
    implementation(project(":cf_netsuite_altavia_erp_connector"))
    implementation(project(":netsuite_altavia_erp_adapter"))
    implementation(project(":notification_gateway"))

}

dependencies {
    implementation("org.springframework.modulith:spring-modulith-api")
    implementation("org.springframework.modulith:spring-modulith-events-api")

    implementation(project(":netsuite_altavia_erp_adapter"))
    implementation(project(":organisation"))
}

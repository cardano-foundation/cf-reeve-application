@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "Accounting Domain Core", allowedDependencies = {
        "notification_gateway", "notification_gateway::domain_core", "notification_gateway::domain_event",
        "organisation", "organisation::domain_core",
        "blockchain_publisher", "blockchain_publisher::domain_core", "blockchain_publisher::domain_event",
})
package org.cardanofoundation.lob.app.accounting_reporting_core;

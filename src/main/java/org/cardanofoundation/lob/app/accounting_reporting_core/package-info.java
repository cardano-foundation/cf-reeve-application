@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "Accounting Service Layer", allowedDependencies = {
        "notification_gateway", "notification_gateway::domain_core", "notification_gateway::domain_event",
        "organisation", "organisation::domain_core",
})
package org.cardanofoundation.lob.app.accounting_reporting_core;

@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "Accounting Service Layer", allowedDependencies = {
        "notification_gateway", "notification_gateway::domain_core", "notification_gateway::domain_event",
        "organisation", "organisation::domain_core", "support::audit_support", "support::crypto_support"
})
package org.cardanofoundation.lob.app.accounting_reporting_core;

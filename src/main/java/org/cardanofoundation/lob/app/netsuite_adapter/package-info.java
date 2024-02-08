@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "Net Suite Adapter", allowedDependencies = {
        "notification_gateway", "notification_gateway::domain_core", "notification_gateway::domain_event",
        "accounting_reporting_core::domain_core", "accounting_reporting_core::domain_event",
        "organisation", "organisation::domain_core", "support::audit_support"
} )
package org.cardanofoundation.lob.app.netsuite_adapter;
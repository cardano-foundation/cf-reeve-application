@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "Blockchain Publisher",
        allowedDependencies =
                { "accounting_reporting_core::domain_core",
                  "accounting_reporting_core::domain_event",
                  "organisation", "organisation::domain_core",
                  "support::audit_support",
                }
)
package org.cardanofoundation.lob.app.blockchain_publisher;

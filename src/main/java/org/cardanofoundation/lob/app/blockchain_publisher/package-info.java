@org.springframework.lang.NonNullApi

@org.springframework.modulith.ApplicationModule(displayName = "Blockchain Publisher",
        allowedDependencies =
                { "accounting_reporting_core::domain_core",
                  "accounting_reporting_core::domain_event"
                }
)
package org.cardanofoundation.lob.app.blockchain_publisher;

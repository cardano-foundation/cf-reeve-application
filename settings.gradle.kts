pluginManagement {
 repositories {
  gradlePluginPortal()
  mavenCentral()
 }
}

rootProject.name = "cf-lob"
include (
 ":cf-application",
 ":support",
 ":accounting_reporting_core",
 ":blockchain_publisher",
 ":cf_netsuite_altavia_erp_connector",
 ":netsuite_altavia_erp_adapter",
 ":notification_gateway",
 ":organisation",
)

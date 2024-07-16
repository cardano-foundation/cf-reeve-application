pluginManagement {
 repositories {
  gradlePluginPortal()
  mavenCentral()
 }
}

rootProject.name = "cf-lob"

include (
 ":cf-application",
 ":cf_netsuite_altavia_erp_connector"
)

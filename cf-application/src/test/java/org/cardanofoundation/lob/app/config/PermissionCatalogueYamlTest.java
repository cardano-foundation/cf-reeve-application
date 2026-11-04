package org.cardanofoundation.lob.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;

import org.cardanofoundation.lob.app.support.security.access.PermissionCatalogue;
import org.cardanofoundation.lob.app.support.security.access.PermissionProperties;

/**
 * The platform has no catalogue of its own: without lob.permissions in application.yml nobody gets any permission, and the application
 * would still start. This also guards the names Spring would mangle if they were map keys ("/", "_").
 */
class PermissionCatalogueYamlTest {

    private final PermissionCatalogue catalogue = load();

    @Test
    void bindsTheModulesAndPermissions() {
        assertThat(catalogue.isConfigured()).isTrue();
        assertThat(catalogue.getModules()).containsExactly("transactions", "reporting", "funding", "document_vault");
        assertThat(catalogue.getPermissions()).hasSize(77).contains("document_vault:publish", "document_vault:delete_any", "transactions:view", "reports:publish", "organisations:create");
        assertThat(catalogue.getPlatformGroup()).isEqualTo("platform");
    }

    @Test
    void settingsBelongToTheModuleThatUsesThem() {
        assertThat(catalogue.modulesOf("cost_centers")).containsExactly("transactions");
        assertThat(catalogue.modulesOf("chart_of_accounts")).containsExactlyInAnyOrder("transactions", "reporting");
        assertThat(catalogue.modulesOf("data_explorer")).containsExactly("transactions");
        assertThat(catalogue.modulesOf("organization_details")).isEmpty();
    }

    @Test
    void keepsTheGroupPathsAndRoleNamesIntact() {
        assertThat(catalogue.getOrganisationRole()).isEqualTo("org_member");
        assertThat(catalogue.getGroupLayout())
                .hasSize(24)
                .containsEntry("transactions", "transactions_viewer")
                .containsEntry("transactions/setup/erp", "transactions_erp")
                .containsEntry("funding/project_manager", "funding_project_manager")
                .containsEntry("transactions/dashboard_editor", "transactions_dashboard_editor")
                .containsEntry("transactions/publish_viewer", "transactions_publish_viewer")
                .containsEntry("reporting/publish_viewer", "reports_publish_viewer")
                .containsEntry("document_vault/publisher", "document_vault_publisher")
                .doesNotContainKey("presets/accountant");
    }

    @Test
    void loadsTheCatalogueTheWaySpringBootDoes() {
        new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
                .withUserConfiguration(PermissionCatalogue.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(PermissionCatalogue.class).getPermissions()).hasSize(71);
                });
    }

    private static PermissionCatalogue load() {
        try {
            var sources = new YamlPropertySourceLoader().load("catalogue", new ClassPathResource("application.yml"));
            return new PermissionCatalogue(new Binder(ConfigurationPropertySources.from(sources))
                    .bind("lob.permissions", PermissionProperties.class)
                    .orElseThrow(() -> new IllegalStateException("application.yml has no lob.permissions")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}

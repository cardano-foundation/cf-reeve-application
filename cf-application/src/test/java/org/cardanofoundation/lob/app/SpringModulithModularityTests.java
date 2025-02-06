package org.cardanofoundation.lob.app;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class SpringModulithModularityTests {

    ApplicationModules modules = ApplicationModules.of(LobServiceApp.class);

    @Test
    void showModules() {
        System.out.println("Modules:");
        modules.forEach(System.out::println);
    }

    @Test
    @Disabled // Disabling it for now, since we have a refactoring task to do so
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        new Documenter(modules).writeDocumentation();
    }

}

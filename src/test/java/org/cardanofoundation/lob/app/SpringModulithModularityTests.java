package org.cardanofoundation.lob.app;

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
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        new Documenter(modules).writeDocumentation();
    }

}

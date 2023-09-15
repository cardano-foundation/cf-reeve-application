package org.cardanofoundation.lob.sourceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages ={"org.cardanofoundation.lob.*"})
@ComponentScan("org.cardanofoundation.lob.*")
public class SourceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SourceApiApplication.class, args);
	}

}

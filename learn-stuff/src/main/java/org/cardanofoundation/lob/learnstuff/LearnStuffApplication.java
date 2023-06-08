package org.cardanofoundation.lob.learnstuff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class LearnStuffApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearnStuffApplication.class, args);
	}

}

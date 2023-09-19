package org.cardanofoundation.lob.txsubmitter;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages ={"org.cardanofoundation.lob.*"})
public class TxSubmitterApplication {

	public static void main(String[] args) {
		SpringApplication.run(TxSubmitterApplication.class, args);
	}


}

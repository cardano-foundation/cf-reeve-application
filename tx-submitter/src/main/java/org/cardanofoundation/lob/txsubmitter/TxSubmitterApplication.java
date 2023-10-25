package org.cardanofoundation.lob.txsubmitter;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages ={"org.cardanofoundation.lob.*"})
public class TxSubmitterApplication {

	@Autowired
	ConnectionFactory connectionFactory;

	public static void main(String[] args) {
		SpringApplication.run(TxSubmitterApplication.class, args);
	}
	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	public void selfQueue() {
		Queue txJobs = QueueBuilder.nonDurable("txJobs").build();
		Exchange delay =ExchangeBuilder.directExchange("delay").build();
		amqpAdmin().declareQueue(txJobs);
		amqpAdmin().declareQueue(QueueBuilder.nonDurable("myqueue").build());
		amqpAdmin().declareQueue(QueueBuilder.nonDurable("txCheckUtxo").build());
		amqpAdmin().declareExchange(delay);
		amqpAdmin().declareBinding(BindingBuilder.bind(txJobs).to(delay).with("txJobs").noargs());
	}

}

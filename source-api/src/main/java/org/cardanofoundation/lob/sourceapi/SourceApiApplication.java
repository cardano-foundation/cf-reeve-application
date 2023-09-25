package org.cardanofoundation.lob.sourceapi;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

@SpringBootApplication
@EntityScan(basePackages ={"org.cardanofoundation.lob.*"})
@ComponentScan("org.cardanofoundation.lob.*")
public class SourceApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SourceApiApplication.class, args);
	}

	@Bean
	public Queue queue() {
		return new Queue("myqueue");
	}

	@Bean
	public TopicExchange exchange(){
		return new TopicExchange("The-Exhange");
	}


}

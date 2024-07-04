//package org.cardanofoundation.lob.app.notification_gateway.internal;
//
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.FanoutExchange;
//import org.springframework.amqp.core.Queue;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    @Bean Queue queue() {
//        return new Queue("queue");
//    }
//
//    @Bean
//    public FanoutExchange exchange() {
//        return new FanoutExchange("target");
//    }
//
//    @Bean
//    public Binding binding(Queue queue, FanoutExchange fanoutExchange) {
//        return BindingBuilder.bind(queue).to(fanoutExchange);
//    }
//
//}

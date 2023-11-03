package org.cardanofoundation.lob.txsubmitter.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class RequeueStrategyService {
    @Autowired
    private AmqpAdmin admin;

    @Autowired
    private AmqpTemplate template;

    Integer initialInterval = 0;

    Integer maxAttempts = 100;

    Double maxInterval = 5.0;

    Double multiplier = 1.0;

    public void messageRequeue(Message message) {
        Integer xRetries = null;
        String jobId = new String(message.getBody());
        if (null == (xRetries = message.getMessageProperties().getHeader("x-retries"))) {
            xRetries = 0;
        }
        xRetries += 1;
        log.error("fail: " + jobId + " Retry:" + xRetries);
        if (maxAttempts > xRetries) {
            Double total = (multiplier * xRetries);
            if (maxInterval < total) {
                total = maxInterval;
            }
            message.getMessageProperties().setHeader("x-retries", xRetries);
            template.convertAndSend(delayQueue(total, message.getMessageProperties().getConsumerQueue()), message);
        }
    }

    private String delayQueue(double ttl, String routingKey) {

        ttl *= 1000;

        Integer totalTime = (int) ttl;
        String name = "delay_txJobs_" + totalTime;
        Queue queue = QueueBuilder.durable(name)
                .ttl(totalTime)
                .deadLetterExchange("delay")
                .deadLetterRoutingKey(routingKey)
                .build();
        admin.declareQueue(queue);
        return name;
    }
}

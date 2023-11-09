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

    Double multiplier = 1.5;

    public void messageRequeue(Message message) {

        if (isRetryable(message)) {
            int total = getWaitingTime(message);
            template.convertAndSend(delayQueue(total, message.getMessageProperties().getConsumerQueue()), message);
        }
    }

    private boolean isRetryable(Message message) {
        Integer xRetries = null;

        if (null == (xRetries = message.getMessageProperties().getHeader("x-retries"))) {
            xRetries = 0;
        }

        return maxAttempts > xRetries;
    }

    private int getWaitingTime(Message message) {
        Integer xRetries = null;

        if (null == (xRetries = message.getMessageProperties().getHeader("x-retries"))) {
            xRetries = 0;
        }

        String jobId = new String(message.getBody());

        Double delay = initialInterval + Math.pow(multiplier, xRetries);

        if (delay > maxInterval && 0 != maxInterval) {
            delay = maxInterval;
        }
        xRetries += 1;
        message.getMessageProperties().setHeader("x-retries", xRetries);
        log.error("fail:" + jobId + " Retry:" + xRetries + " Delay:" + delay);
        return (int) Math.ceil(delay);

    }

    private String delayQueue(int ttl, String routingKey) {

        ttl *= 1000;

        Integer totalTime = ttl;

        String name = "delay_" + routingKey + "_" + totalTime;
        Queue queue = QueueBuilder.durable(name)
                .ttl(totalTime)
                .deadLetterExchange("delay")
                .deadLetterRoutingKey(routingKey)
                .expires(totalTime * 2)
                .build();
        admin.declareQueue(queue);
        return name;
    }
}

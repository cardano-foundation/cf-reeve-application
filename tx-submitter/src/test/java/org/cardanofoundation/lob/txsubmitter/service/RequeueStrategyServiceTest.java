package org.cardanofoundation.lob.txsubmitter.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RequeueStrategyServiceTest {

    @InjectMocks
    RequeueStrategyService requeueStrategy;
    @Mock
    private AmqpAdmin admin;

    @Mock
    AmqpTemplate template;


    @Test
    void messageRequeue() {
        Message message = Mockito.mock(Message.class);
        MessageProperties messageProperties = Mockito.mock(MessageProperties.class);
        Mockito.when(message.getMessageProperties()).thenReturn(messageProperties);
        Mockito.when(message.getBody()).thenReturn("100".getBytes());
        Mockito.when(messageProperties.getHeader("x-retries")).thenReturn(null);

        requeueStrategy.messageRequeue(message);

        Mockito.verify(messageProperties, Mockito.times(1)).setHeader("x-retries", 1);
        Mockito.verify(template,Mockito.times(1)).convertAndSend("delay_txJobs_1000",message);
    }

    @Test
    void messageRequeueRetry() {
        Message message = Mockito.mock(Message.class);
        MessageProperties messageProperties = Mockito.mock(MessageProperties.class);
        Mockito.when(message.getMessageProperties()).thenReturn(messageProperties);
        Mockito.when(message.getBody()).thenReturn("100".getBytes());
        Mockito.when(messageProperties.getHeader("x-retries")).thenReturn(3);

        requeueStrategy.messageRequeue(message);

        Mockito.verify(messageProperties, Mockito.times(1)).setHeader("x-retries", 4);
        Mockito.verify(template,Mockito.times(1)).convertAndSend("delay_txJobs_4000",message);
    }

    @Test
    void messageRequeueRetryTop() {
        Message message = Mockito.mock(Message.class);
        MessageProperties messageProperties = Mockito.mock(MessageProperties.class);
        Mockito.when(message.getMessageProperties()).thenReturn(messageProperties);
        Mockito.when(message.getBody()).thenReturn("100".getBytes());
        Mockito.when(messageProperties.getHeader("x-retries")).thenReturn(4);

        requeueStrategy.multiplier = 1.5;
        requeueStrategy.maxInterval=5.0;
        requeueStrategy.messageRequeue(message);

        Mockito.verify(messageProperties, Mockito.times(1)).setHeader("x-retries", 5);
        Mockito.verify(template,Mockito.times(1)).convertAndSend("delay_txJobs_5000",message);
    }


    @Test
    void messageRequeueRetryMaxInterval() {
        Message message = Mockito.mock(Message.class);
        MessageProperties messageProperties = Mockito.mock(MessageProperties.class);
        Mockito.when(message.getMessageProperties()).thenReturn(messageProperties);
        Mockito.when(message.getBody()).thenReturn("100".getBytes());
        Mockito.when(messageProperties.getHeader("x-retries")).thenReturn(5);

        requeueStrategy.messageRequeue(message);

        Mockito.verify(messageProperties, Mockito.times(1)).setHeader("x-retries", 6);
        Mockito.verify(template,Mockito.times(1)).convertAndSend("delay_txJobs_5000",message);
    }

    @Test
    void messageRequeueLastRetry() {
        Message message = Mockito.mock(Message.class);
        MessageProperties messageProperties = Mockito.mock(MessageProperties.class);
        Mockito.when(message.getMessageProperties()).thenReturn(messageProperties);
        Mockito.when(message.getBody()).thenReturn("100".getBytes());
        Mockito.when(messageProperties.getHeader("x-retries")).thenReturn(99);

        requeueStrategy.messageRequeue(message);

        Mockito.verify(messageProperties, Mockito.never()).setHeader(Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(template,Mockito.never()).convertAndSend(Mockito.anyString(),Mockito.any(Message.class));
    }
}
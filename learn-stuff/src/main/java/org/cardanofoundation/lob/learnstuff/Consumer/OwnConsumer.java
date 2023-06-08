package org.cardanofoundation.lob.learnstuff.Consumer;

import org.cardanofoundation.lob.learnstuff.entity.Person;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OwnConsumer {
    public static final String TOPIC = "message";
    public static final String GROUP_ID = "GroupId";
    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void processMessage(Person person){
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("COMPLETO Message received: " + person.getEmail() + " -> " + person.getName());
        System.out.println("EN: " + person.getDate());

    }
}

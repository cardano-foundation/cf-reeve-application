package org.cardanofoundation.lob.learnstuff.Producer;

import org.cardanofoundation.lob.learnstuff.Consumer.OwnConsumer;
import org.cardanofoundation.lob.learnstuff.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OwnProducer {

    private final KafkaTemplate<String, Person> kafkaTemplate;

    public OwnProducer(KafkaTemplate<String, Person> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Person person) {
        kafkaTemplate.send(OwnConsumer.TOPIC, person);
    }
}

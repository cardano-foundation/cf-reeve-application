package org.cardanofoundation.lob.learnstuff.controller;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.cardanofoundation.lob.learnstuff.Producer.OwnProducer;
import org.cardanofoundation.lob.learnstuff.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class FirstController {

    @Autowired
    OwnProducer producer;

    @RequestMapping(value = "/prueba", method = RequestMethod.GET)
    public ResponseEntity<Person> index() {
        Person person = new Person();
        person.setName("nombre");
        person.setDate(new Date());
        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @RequestMapping(value = "/products", method = RequestMethod.POST)
    public ResponseEntity<Object> createProduct(@Validated @RequestBody Person person) {
        try {
            producer.send(person);
        } catch (Exception exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(person, HttpStatus.CREATED);
    }

}

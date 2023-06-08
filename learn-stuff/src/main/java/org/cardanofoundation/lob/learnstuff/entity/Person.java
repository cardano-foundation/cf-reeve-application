package org.cardanofoundation.lob.learnstuff.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import org.apache.kafka.common.protocol.types.Field;

import java.util.Date;

public class Person {
    @NotEmpty
    private String name;

    @NotEmpty
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss")
    private Date date = new Date();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

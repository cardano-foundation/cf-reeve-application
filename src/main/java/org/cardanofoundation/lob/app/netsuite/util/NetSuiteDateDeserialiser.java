package org.cardanofoundation.lob.app.netsuite.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NetSuiteDateDeserialiser extends LocalDateTimeDeserializer {

    private final static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a");

public NetSuiteDateDeserialiser() {
        super(DTF);
    }

    @Override
    protected LocalDateTime _fromString(JsonParser p, DeserializationContext ctxt, String string0) throws IOException {
        return super._fromString(p, ctxt, string0.toLowerCase());
    }

}

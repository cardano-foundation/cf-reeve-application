package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

public class ACLMappingException extends RuntimeException {

    public ACLMappingException(String message) {
        super(message);
    }

    public ACLMappingException(String message, Throwable cause) {
        super(message, cause);
    }

}

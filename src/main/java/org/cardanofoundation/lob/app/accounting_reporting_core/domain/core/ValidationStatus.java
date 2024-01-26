package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

public enum ValidationStatus {

    NOT_VALIDATED,

    CLEANSED_SUCCESS,

    CLEANSED_FAILED,

    BUSINESS_RULES_VALIDATION_FAILED,

    BUSINESS_RULES_VALIDATION_SUCCESS,

    CONVERSION_FAILED,

    CONVERSION_SUCCESS,

    VALIDATED

}

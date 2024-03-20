package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

public enum BatchStatus {

    STARTED, // job processing started

    FINISHED, // all transactions processed and saved

    SETTLED, // all VALID transactions that passed business validation are settled to the blockchain

    FINALIZED // all transactions are settled and the transactions are finalized on the blockchain

}

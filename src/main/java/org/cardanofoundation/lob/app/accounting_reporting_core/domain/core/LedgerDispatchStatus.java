package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

public enum LedgerDispatchStatus {
    NOT_DISPATCHED, // not dispatched to blockchain(s) yet

    STORED, // acking that we stored in the database

    DISPATCHED, // dispatched to blockchain(s)

    COMPLETED,

    FINALIZED; // finalised on blockchain(s)

    /**
     * Dispatchable means that we can dispatch the transaction line to the blockchain(s)
     */
    public boolean isDispatchable() {
        return this == NOT_DISPATCHED;
    }

}

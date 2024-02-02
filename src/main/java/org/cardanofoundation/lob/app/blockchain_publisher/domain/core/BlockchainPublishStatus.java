package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

public enum BlockchainPublishStatus {

    STORED,

    SUBMITTED, // submitted and setting in the mem pool for now

    VISIBLE_ON_CHAIN, // confirmed to be visible on-chain

    COMPLETED, // confirmed on-chain and a few blocks passed

    ROLLBACKED, // signal to resubmit the transaction since it disappeared from on chain

    FINALIZED,

}

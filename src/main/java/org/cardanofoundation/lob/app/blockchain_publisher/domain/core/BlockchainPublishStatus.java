package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

public enum BlockchainPublishStatus {

    SAVE_ACK,

    SUBMITTED, // submitted and setting in the mem pool for now

    CONFIRMED, // confirmed to be visible on-chain

    COMPLETED, // confirmed on-chain and a few blocks passed

    FINALIZED,

}

package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

public enum BlockchainPublishStatus {

    SUBMITTED,

    CONFIRMED, // confirmed onc-hain

    COMPLETED, // confirmed on-chain and a few blocks passed

    FINALIZED,

}

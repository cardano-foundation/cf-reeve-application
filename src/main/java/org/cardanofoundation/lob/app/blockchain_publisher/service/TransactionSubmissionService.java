package org.cardanofoundation.lob.app.blockchain_publisher.service;

public interface TransactionSubmissionService {

    /**
     * Submit transaction and return transaction hash.
     *
     * @param txData
     * @return transaction hash
     */
    String submitTransaction(byte[] txData);

}

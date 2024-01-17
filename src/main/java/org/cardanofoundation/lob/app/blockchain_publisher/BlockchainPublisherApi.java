package org.cardanofoundation.lob.app.blockchain_publisher;

import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class BlockchainPublisherApi {

    // TODO total fake implementation for now (!) but it established initial contract
//    public boolean isPublished(String internalTransactionNumber, String transactionLineItemId) {
//        val lastChar = internalTransactionNumber.charAt(transactionLineItemId.length() - 1);
//
//        return lastChar % 2 == 0;
//    }

    public boolean isPublished(String transactionNumber, String transactionLineItemId) {
        return false;
    }

}

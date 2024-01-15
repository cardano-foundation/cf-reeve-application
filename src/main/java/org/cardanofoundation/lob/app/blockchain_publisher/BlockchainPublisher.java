package org.cardanofoundation.lob.app.blockchain_publisher;

import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class BlockchainPublisher {

    public boolean isPublished(String transactionLineItemId) {
        val lastChar = transactionLineItemId.charAt(transactionLineItemId.length() - 1);

        return Character.isDigit(lastChar);
    }

}

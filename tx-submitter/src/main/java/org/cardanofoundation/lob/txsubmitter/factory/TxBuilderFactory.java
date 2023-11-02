package org.cardanofoundation.lob.txsubmitter.factory;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.springframework.stereotype.Service;

@Service
public class TxBuilderFactory {
    public QuickTxBuilder createTxBuilder(BackendService backendService) {
        return new QuickTxBuilder(backendService);
    }

    public Tx createTx(Account sender, TxSubmitJob txSubmitJob, final double nonce) {
        return new Tx()
                .payToAddress(sender.baseAddress(), Amount.ada(1.5 + nonce * 0.001))
                .attachMetadata(CBORMetadata.deserialize(txSubmitJob.getTransactionMetadata()))
                .from(sender.baseAddress());
    }

    public double createRandom() {
        return Math.random();
    }
}

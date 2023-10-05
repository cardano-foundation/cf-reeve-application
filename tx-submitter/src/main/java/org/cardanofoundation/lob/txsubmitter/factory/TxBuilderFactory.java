package org.cardanofoundation.lob.txsubmitter.factory;

import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;

public class TxBuilderFactory {
    public QuickTxBuilder createTxBuilder(BackendService backendService) {
        return new QuickTxBuilder(backendService);
    }
}

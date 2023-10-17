package org.cardanofoundation.lob.txsubmitter.factory;

import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TxBuilderFactoryTest {

    @Test
    void createTxBuilder() {
        BackendService backendService = Mockito.mock(BackendService.class);
        TxBuilderFactory txBuilderFactory = new TxBuilderFactory();
        QuickTxBuilder quickTxBuilder = txBuilderFactory.createTxBuilder(backendService);
        Assertions.assertInstanceOf(QuickTxBuilder.class, quickTxBuilder);

    }
}
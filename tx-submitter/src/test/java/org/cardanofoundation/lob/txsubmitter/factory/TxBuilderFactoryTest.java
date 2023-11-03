package org.cardanofoundation.lob.txsubmitter.factory;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.quicktx.AbstractTx;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TxBuilderFactoryTest {

    @Test
    void createTxBuilder() {
        BackendService backendService = Mockito.mock(BackendService.class);
        TxBuilderFactory txBuilderFactory = new TxBuilderFactory();
        QuickTxBuilder quickTxBuilder = txBuilderFactory.createTxBuilder(backendService);
        Assertions.assertInstanceOf(QuickTxBuilder.class, quickTxBuilder);

    }

    @Test
    void createTx() {
        Account sender = Mockito.mock(Account.class);
        TxSubmitJob txSubmitJob = Mockito.mock(TxSubmitJob.class);
        double random = Math.random();

        Mockito.when(sender.baseAddress()).thenReturn("addr_test1qrq699e8nd9e8jw93kkunh9vmuucaq2qth0xym42ws6sqs468gt7ad8l6dnr9yglcqt5f6ss63wgntfjyl2xpxw0wqpq9xr8tx");
        Metadata metadata = MetadataBuilder.createMetadata();
        Mockito.when(txSubmitJob.getTransactionMetadata()).thenReturn(metadata.serialize());

        TxBuilderFactory txBuilderFactory = new TxBuilderFactory();
        Assertions.assertInstanceOf(AbstractTx.class, txBuilderFactory.createTx(sender, txSubmitJob, random));
    }

    @Test
    void createRandom() {
        TxBuilderFactory txBuilderFactory = new TxBuilderFactory();
        double randomResult = txBuilderFactory.createRandom();
        Assertions.assertEquals(Math.random(), randomResult, 1);
        Assertions.assertNotEquals(0.0d, randomResult);
    }
}
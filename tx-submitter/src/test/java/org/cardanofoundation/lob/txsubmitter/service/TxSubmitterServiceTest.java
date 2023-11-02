package org.cardanofoundation.lob.txsubmitter.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.ProtocolParams;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.UtxoService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFEpochService;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import org.aspectj.lang.annotation.Before;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJob;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJobStatus;
import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.cardanofoundation.lob.common.model.TxSubmitJobStatus;
import org.cardanofoundation.lob.txsubmitter.factory.TxBuilderFactory;
import org.cardanofoundation.lob.txsubmitter.repository.LedgerEventRegistrationRepository;
import org.cardanofoundation.lob.txsubmitter.repository.TxSubmitJobRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TxSubmitterServiceTest {
    @InjectMocks
    TxSubmitterService txSubmitterService;

    @Mock
    private BackendService backendService;
    @Mock
    private Account sender;
    @Mock
    private LedgerEventRegistrationRepository ledgerEventRegistrationRepository;
    @Mock
    private TxBuilderFactory txBuilderFactory;
    @Mock
    private ServiceTxPackaging serviceTxPackaging;
    @Mock
    private TxSubmitJobRepository txSubmitJobRepository;

    @Mock
    private AmqpAdmin admin;
    @Mock
    private AmqpTemplate template;

    @Test
    void listen() {
        LedgerEventRegistrationJob registrationJob = new LedgerEventRegistrationJob();
        TxSubmitJob txSubmitJob = new TxSubmitJob();
        txSubmitJob.setId(4);
        Mockito.when(ledgerEventRegistrationRepository.findById(Mockito.anyString())).thenReturn(Optional.of(registrationJob));
        List<TxSubmitJob> txSubmitJobs = Arrays.asList(txSubmitJob);
        Mockito.when(serviceTxPackaging.createTxJobs(registrationJob)).thenReturn(txSubmitJobs);
        txSubmitterService.listen("4");
        Mockito.verify(ledgerEventRegistrationRepository, Mockito.times(1)).save(registrationJob);
        Assertions.assertEquals(LedgerEventRegistrationJobStatus.PROCESSED, registrationJob.getJobStatus());
        Mockito.verify(template, Mockito.times(1)).convertAndSend(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void listenException() throws ResponseStatusException {
        LedgerEventRegistrationJob registrationJob = new LedgerEventRegistrationJob();
        TxSubmitJob txSubmitJob = new TxSubmitJob();
        txSubmitJob.setId(4);
        Mockito.when(ledgerEventRegistrationRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());

        Assertions.assertThrows(ResponseStatusException.class, () -> txSubmitterService.listen("4"));
        //Mockito.verify(template, Mockito.times(1)).convertAndSend(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void listenTwoSubmit() throws Exception {
        UtxoService utxoService = Mockito.mock(UtxoService.class);
        QuickTxBuilder quickTxBuilder = Mockito.mock(QuickTxBuilder.class);
        Result result = Mockito.mock(Result.class);
        QuickTxBuilder.TxContext txContext = Mockito.mock(QuickTxBuilder.TxContext.class);
        Mockito.when(txBuilderFactory.createTxBuilder(backendService)).thenReturn(quickTxBuilder);
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.getBody()).thenReturn("100".getBytes());

        Mockito.when(quickTxBuilder.compose(Mockito.any())).thenReturn(txContext);
        Mockito.when(txContext.withSigner(Mockito.any())).thenReturn(txContext);
        Mockito.when(result.isSuccessful()).thenReturn(true);
        Mockito.when(txContext.complete()).thenReturn(result);
        Mockito.when(result.getValue()).thenReturn("cc95221752e81d08113d5d995f1804276d4b5c6882236da1d96870829a1fbafa");
        final TxSubmitJob txSubmitJob = Mockito.mock(TxSubmitJob.class);
        Metadata metadata = MetadataBuilder.createMetadata();
        Mockito.when(txSubmitJob.getTransactionMetadata()).thenReturn(metadata.serialize());
        Mockito.when(txSubmitJob.getJobStatus()).thenReturn(TxSubmitJobStatus.PENDING);
        Mockito.when(txSubmitJob.getId()).thenReturn(6);

        Mockito.when(txSubmitJobRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(txSubmitJob));

        txSubmitterService.listenTwo(message);

        Mockito.verify(txSubmitJobRepository, Mockito.times(1)).save(txSubmitJob);
        Mockito.verify(txSubmitJob, Mockito.times(1)).setTransactionId("cc95221752e81d08113d5d995f1804276d4b5c6882236da1d96870829a1fbafa");
        Mockito.verify(txSubmitJob, Mockito.times(1)).setJobStatus(TxSubmitJobStatus.SUBMITTED);
        Mockito.verify(template, Mockito.times(1)).convertAndSend("txCheckUtxo", 6);

    }

    @Test
    void listenTwoFail() throws Exception {
        UtxoService utxoService = Mockito.mock(UtxoService.class);
        QuickTxBuilder quickTxBuilder = Mockito.mock(QuickTxBuilder.class);
        Result result = Mockito.mock(Result.class);
        QuickTxBuilder.TxContext txContext = Mockito.mock(QuickTxBuilder.TxContext.class);
        Mockito.when(txBuilderFactory.createTxBuilder(backendService)).thenReturn(quickTxBuilder);
        double random = Math.random();
        Mockito.when(txBuilderFactory.createRandom()).thenReturn(random);

        Mockito.when(quickTxBuilder.compose(Mockito.any())).thenReturn(txContext);
        Mockito.when(txContext.withSigner(Mockito.any())).thenReturn(txContext);
        Mockito.when(result.isSuccessful()).thenReturn(false);
        Mockito.when(txContext.complete()).thenReturn(result);
        TxSubmitJob txSubmitJob = Mockito.mock(TxSubmitJob.class);
        Tx tx = Mockito.mock(Tx.class);
        Mockito.when(txBuilderFactory.createTx(sender, txSubmitJob, random)).thenReturn(tx);
        Metadata metadata = MetadataBuilder.createMetadata();
        Mockito.when(txSubmitJob.getTransactionMetadata()).thenReturn(metadata.serialize());
        Mockito.when(txSubmitJob.getJobStatus()).thenReturn(TxSubmitJobStatus.PENDING);
        Mockito.when(txSubmitJobRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(txSubmitJob));
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.getBody()).thenReturn("100".getBytes());
        MessageProperties messageProperties = Mockito.mock(MessageProperties.class);
        Mockito.when(message.getMessageProperties()).thenReturn(messageProperties);
        Mockito.when(messageProperties.getHeader("x-retries")).thenReturn(null);
        txSubmitterService.listenTwo(message);

        Mockito.verify(txSubmitJobRepository, Mockito.times(2)).save(Mockito.any(TxSubmitJob.class));
        Mockito.verify(txSubmitJob, Mockito.times(1)).setJobStatus(TxSubmitJobStatus.FAILED);
        Mockito.verify(messageProperties, Mockito.times(1)).setHeader("x-retries", 1);
        Mockito.verify(template, Mockito.times(1)).convertAndSend("delay_txJobs_1000", message);

    }

    @Test
    void listenTwoSubmittedAlready() throws Exception {
        TxSubmitJob txSubmitJob = Mockito.mock(TxSubmitJob.class);
        Mockito.when(txSubmitJob.getTransactionMetadata()).thenReturn("Trans".getBytes());
        Mockito.when(txSubmitJob.getJobStatus()).thenReturn(TxSubmitJobStatus.SUBMITTED);
        Mockito.when(txSubmitJobRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(txSubmitJob));
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.getBody()).thenReturn("100".getBytes());
        txSubmitterService.listenTwo(message);
        Mockito.verify(txSubmitJobRepository, Mockito.never()).save(Mockito.any(TxSubmitJob.class));
    }

    @Test
    void listenTwoDoesntExist() throws Exception {
        Mockito.when(txSubmitJobRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.getBody()).thenReturn("100".getBytes());
        txSubmitterService.listenTwo(message);
        Mockito.verify(txSubmitJobRepository, Mockito.never()).save(Mockito.any(TxSubmitJob.class));
    }

    @Test
    void processTxSubmitJob() {
    }
}

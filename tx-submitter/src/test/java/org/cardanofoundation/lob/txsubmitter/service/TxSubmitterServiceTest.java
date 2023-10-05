package org.cardanofoundation.lob.txsubmitter.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.ProtocolParams;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFEpochService;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
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
import org.springframework.amqp.core.AmqpTemplate;
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
        Mockito.verify(template, Mockito.times(1)).convertAndSend(Mockito.anyString(), Mockito.anyInt());
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
    void listenTwoFail() throws Exception {
        Mockito.when(sender.baseAddress()).thenReturn("PAJASTOSTAS");

        TxSubmitJob txSubmitJob = Mockito.mock(TxSubmitJob.class);
        Metadata metadata = MetadataBuilder.createMetadata();
        Mockito.when(txSubmitJob.getTransactionMetadata()).thenReturn(metadata.serialize());
        Mockito.when(txSubmitJob.getJobStatus()).thenReturn(TxSubmitJobStatus.PENDING);
        Mockito.when(txSubmitJobRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(txSubmitJob));
        Assertions.assertThrows(RuntimeException.class, () -> txSubmitterService.listenTwo("100"));

        Mockito.verify(txSubmitJobRepository, Mockito.times(1)).save(Mockito.any(TxSubmitJob.class));
    }

    @Test
    void listenTwoSubmittedAlready() throws Exception {
        TxSubmitJob txSubmitJob = Mockito.mock(TxSubmitJob.class);
        Mockito.when(txSubmitJob.getTransactionMetadata()).thenReturn("Trans".getBytes());
        Mockito.when(txSubmitJob.getJobStatus()).thenReturn(TxSubmitJobStatus.SUBMITTED);
        Mockito.when(txSubmitJobRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(txSubmitJob));
        txSubmitterService.listenTwo("100");
        Mockito.verify(txSubmitJobRepository, Mockito.never()).save(Mockito.any(TxSubmitJob.class));
    }

    @Test
    void listenTwoDoesntExist() throws Exception {
        Mockito.when(txSubmitJobRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        txSubmitterService.listenTwo("100");
        Mockito.verify(txSubmitJobRepository, Mockito.never()).save(Mockito.any(TxSubmitJob.class));
    }

    @Test
    void processTxSubmitJob() {
    }
}
package org.cardanofoundation.lob.txsubmitter.service;

import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJob;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJobStatus;
import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.cardanofoundation.lob.txsubmitter.repository.LedgerEventRegistrationRepository;
import org.cardanofoundation.lob.txsubmitter.repository.TxSubmitJobRepository;
import org.junit.jupiter.api.Assertions;
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
    private LedgerEventRegistrationRepository ledgerEventRegistrationRepository;

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
    void listenTwo() {
    }

    @Test
    void processTxSubmitJob() {
    }
}
package org.cardanofoundation.lob.txsubmitter.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.*;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.crypto.Hashing;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJob;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJobStatus;
import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.cardanofoundation.lob.common.model.TxSubmitJobStatus;
import org.cardanofoundation.lob.txsubmitter.factory.TxBuilderFactory;
import org.cardanofoundation.lob.txsubmitter.repository.LedgerEventRegistrationRepository;
import org.cardanofoundation.lob.txsubmitter.repository.LedgerEventRepository;
import org.cardanofoundation.lob.txsubmitter.repository.TxSubmitJobRepository;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Component
@Log4j2
public class TxSubmitterService {

    private BackendService backendService;

    private Account sender;

    @Autowired
    private AmqpAdmin admin;
    @Autowired
    private TxBuilderFactory txBuilderFactory;
    @Autowired
    private ServiceTxPackaging serviceTxPackaging;
    @Autowired
    private LedgerEventRegistrationRepository ledgerEventRegistrationRepository;

    @Autowired
    private LedgerEventRepository ledgerEventRepository;

    @Autowired
    private TxSubmitJobRepository txSubmitJobRepository;

    @Autowired
    private AmqpTemplate template;

    @PostConstruct
    public void init() {
        backendService = new BFBackendService("http://localhost:8080/api/v1/", "Dummy");
        final String senderMnemonic = "omit patch shoe tunnel fluid inform mom mandate glare balance bachelor sense market question oval talk damp void play retire fold attract execute tomato";
        sender = new Account(Networks.testnet(), senderMnemonic);

    }

    @RabbitListener(queues = "myqueue")
    public void listen(String registrationId) {
        log.info(registrationId);
        final LedgerEventRegistrationJob registrationJob = ledgerEventRegistrationRepository.findById(registrationId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration has already been approved."));
        final List<TxSubmitJob> txSubmitJobs = serviceTxPackaging.createTxJobs(registrationJob);
        txSubmitJobRepository.saveAll(txSubmitJobs);

        registrationJob.setJobStatus(LedgerEventRegistrationJobStatus.PROCESSED);
        ledgerEventRegistrationRepository.save(registrationJob);

        for (final TxSubmitJob txSubmitJob : txSubmitJobs) {
            template.convertAndSend("txJobs", txSubmitJob.getId().toString());
        }

        txSubmitJobRepository.saveAll(txSubmitJobs);
    }

    /**
     * @// TODO: 26/10/2023 How to declare a queue names constan?
     * @param message
     * @throws Exception
     */
    @RabbitListener(queues = "txJobs", concurrency = "5"/*, ackMode = "MANUAL"*/)
    public void listenTwo(final Message message) throws Exception {

        String jobId = new String(message.getBody());
        TxSubmitJob txSubmitJob = txSubmitJobRepository.findById(Integer.valueOf(jobId)).orElse(null);

        if (null == txSubmitJob) {
            log.info("Doesn't exist: " + jobId);
            return;
        }

        log.info("Processing: " + txSubmitJob.getTransactionMetadata().length + " -- " + Hashing.blake2b256Hex(txSubmitJob.getTransactionMetadata()));

        if (TxSubmitJobStatus.SUBMITTED == txSubmitJob.getJobStatus() || TxSubmitJobStatus.CONFIRMED == txSubmitJob.getJobStatus()) {
            log.info("Submitted already: " + jobId);
            return;
        }
        processTxSubmitJob(txSubmitJob, Math.random()).ifPresentOrElse(
                (txId) -> {
                    log.info("tx Id is: " + txId + " -- " + Hashing.blake2b256Hex(txSubmitJob.getTransactionMetadata()));
                    txSubmitJob.setTransactionId(txId);
                    txSubmitJob.setJobStatus(TxSubmitJobStatus.SUBMITTED);
                    log.info("Submit: " + jobId);
                    template.convertAndSend("txCheckUtxo", txSubmitJob.getId());
                },
                () -> {
                    txSubmitJob.setJobStatus(TxSubmitJobStatus.FAILED);

                    txSubmitJobRepository.save(txSubmitJob);

                    messageRequeue(message);
                    return;
                    //throw new RuntimeException("Something went wrong");
                }
        );


        txSubmitJobRepository.save(txSubmitJob);


    }

    public String delayQueue(Integer ttl) {

        String name = "delay_txJobs_" + ttl.toString();

        Queue queue = QueueBuilder.nonDurable(name)
                .ttl(ttl)
                .deadLetterExchange("delay")
                .deadLetterRoutingKey("txJobs")
                .build();
        admin.declareQueue(queue);
        return name;
    }

    public void messageRequeue(Message message){
        Integer value = null;
        String jobId = new String(message.getBody());
        if (null == (value = message.getMessageProperties().getHeader("x-retries"))) {
            value = 0;
        }
        value += 1;

        log.error("fail: " + jobId+ " Retry:" + value) ;
        if (100 > value) {
            Integer total = (1000 * value);
            if(5000 < total){
                total = 5000;
            }
            message.getMessageProperties().setHeader("x-retries", value);
            template.convertAndSend(delayQueue(total), message);
        }
    }
    public Optional<String> processTxSubmitJob(final TxSubmitJob txSubmitJob, final double nonce) {

        final QuickTxBuilder quickTxBuilder = txBuilderFactory.createTxBuilder(backendService);
        final Tx tx = txBuilderFactory.createTx(sender,txSubmitJob, nonce);

        Result<String> result = null;
        try {
            result = quickTxBuilder.compose(tx)
                    .withSigner(SignerProviders.signerFrom(sender)).complete();

            //waitForTransaction(result);
            //checkIfUtxoAvailable(result.getValue(), sender.baseAddress());
        } catch (Exception e) {
            return Optional.empty();
        }

        if (result.isSuccessful()) {
            return Optional.of(result.getValue());
        } else {
            return Optional.empty();
        }
    }

    private void waitForTransaction(final Result<String> result) {
        try {
            if (result.isSuccessful()) { //Wait for transaction to be mined
                int count = 0;
                while (count < 60) {
                    final Result<TransactionContent> txnResult = backendService.getTransactionService().getTransaction(result.getValue());
                    if (txnResult.isSuccessful()) {
                        break;
                    } else {
                        log.debug("Waiting for transaction to be mined ....");
                    }

                    count++;
                    Thread.sleep(100);
                }
            }
        } catch (final Exception e) {
            log.error(e);
        }
    }

}

package org.cardanofoundation.lob.txsubmitter.service;


import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.common.model.Networks;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.crypto.Hashing;
import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.cardanofoundation.lob.common.model.TxSubmitJobStatus;
import org.cardanofoundation.lob.txsubmitter.repository.TxSubmitJobRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Log4j2
public class TxCheckUtxo {
    private BackendService backendService;

    private Account sender;
    @Autowired
    private TxSubmitJobRepository txSubmitJobRepository;

    @PostConstruct
    public void init() {
        backendService = new BFBackendService("http://localhost:8080/api/v1/", "Dummy");
        final String senderMnemonic = "omit patch shoe tunnel fluid inform mom mandate glare balance bachelor sense market question oval talk damp void play retire fold attract execute tomato";
        sender = new Account(Networks.testnet(), senderMnemonic);

    }

    @RabbitListener(queues = "txCheckUtxo", concurrency = "1", batch = "1")
    private void checkIfUtxoAvailable(String jobId) {

        TxSubmitJob txSubmitJob = txSubmitJobRepository.findById(Integer.valueOf(jobId)).orElse(null);
        if (null == txSubmitJob) {
            log.info("Doesn't exist: " + jobId);
            return;
        }
        log.info("Confirming: " + txSubmitJob.getId() + " -- " + txSubmitJob.getTransactionId() + " -- " + Hashing.blake2b256Hex(txSubmitJob.getTransactionMetadata()));

        if (TxSubmitJobStatus.CONFIRMED == txSubmitJob.getJobStatus()) {
            log.info("Confirmed already: " + jobId);
            return;
        }
        Optional<Utxo> utxo = Optional.empty();

        try {
            utxo = new DefaultUtxoSupplier(backendService.getUtxoService()).getTxOutput(txSubmitJob.getTransactionId(), 0);
            //final List<Utxo> utxos = new DefaultUtxoSupplier(backendService.getUtxoService()).getAll(sender.baseAddress());
            //log.info(utxos);
            //utxo = utxos.stream().filter(u -> u.getTxHash().equals(txSubmitJob.getTransactionId())).findFirst();
            log.debug("Try to get new output... txhash: " + txSubmitJob.getTransactionId());
        } catch (Exception e) {
            log.error("Something Wrong: " + e.getMessage());
            throw new RuntimeException("Something Wrong: " + e.getMessage());
        }

        if (utxo.isEmpty()) {
            log.error("Need more time to check: " + jobId);
            throw new RuntimeException("Need more time to check");
        }
        log.info("tx Id is: " + txSubmitJob.getTransactionId() + " -- " + Hashing.blake2b256Hex(txSubmitJob.getTransactionMetadata()));
        txSubmitJob.setJobStatus(TxSubmitJobStatus.CONFIRMED);
        txSubmitJobRepository.save(txSubmitJob);
        log.info("Confirmed: " + jobId);
    }
}

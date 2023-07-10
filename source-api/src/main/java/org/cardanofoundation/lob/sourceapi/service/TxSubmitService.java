package org.cardanofoundation.lob.sourceapi.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
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
import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Log4j2
public class TxSubmitService {

    private BackendService backendService;

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
                    Thread.sleep(2000);
                }
            }
        } catch (final Exception e) {
            log.error(e);
        }
    }

    private void checkIfUtxoAvailable(final String txHash, final String address) {
        Optional<Utxo> utxo = Optional.empty();
        int count = 0;
        while (utxo.isEmpty()) {
            if (count++ >= 20)
                break;
            final List<Utxo> utxos = new DefaultUtxoSupplier(backendService.getUtxoService()).getAll(address);
            utxo = utxos.stream().filter(u -> u.getTxHash().equals(txHash)).findFirst();
            log.debug("Try to get new output... txhash: " + txHash);
            try {
                Thread.sleep(1000);
            } catch (final Exception e) {
                log.error(e);
            }
        }
    }

    @PostConstruct
    public void init() {
        backendService = new BFBackendService("http://localhost:8080/api/v1/", "Dummy");
    }

    public Optional<String> processTxSubmitJob(final TxSubmitJob txSubmitJob, final double nonce) {
        final String senderMnemonic = "omit patch shoe tunnel fluid inform mom mandate glare balance bachelor sense market question oval talk damp void play retire fold attract execute tomato";
        final Account sender = new Account(Networks.testnet(), senderMnemonic);

        final QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        final Tx tx = new Tx()
                .payToAddress(sender.baseAddress(), Amount.ada(1.5 + nonce * 0.001))
                .attachMetadata(CBORMetadata.deserialize(txSubmitJob.getTransactionMetadata()))
                .from(sender.baseAddress());

        Result<String> result = null;
        try {
            result = quickTxBuilder.compose(tx)
                    .withSigner(SignerProviders.signerFrom(sender))                    .complete();


            waitForTransaction(result);
            checkIfUtxoAvailable(result.getValue(), sender.baseAddress());
        } catch (Exception e) {
            return Optional.empty();
        }


        if (result.isSuccessful()) {
            return Optional.of(result.getValue());
        } else {
            return Optional.empty();
        }
    }
}

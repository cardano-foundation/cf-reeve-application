package org.cardanofoundation.lob.sourceapi.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.*;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.cbor.CBORMetadata;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Log4j2
public class TxSubmitService {

    private BackendService backendService;

    @PostConstruct
    public void init() {
        backendService = new BFBackendService("http://localhost:8080/api/v1/", "Dummy");
    }

    public Optional<String> processTxSubmitJob(final TxSubmitJob txSubmitJob) {
        final String senderMnemonic = "omit patch shoe tunnel fluid inform mom mandate glare balance bachelor sense market question oval talk damp void play retire fold attract execute tomato";
        final Account sender = new Account(Networks.testnet(), senderMnemonic);

        final QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        final Tx tx = new Tx()
                .payToAddress(sender.baseAddress(), Amount.ada(1.5))
                .attachMetadata(CBORMetadata.deserialize(txSubmitJob.getTransactionMetadata()))
                .from(sender.baseAddress());

        final Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender))
                .complete();

        if (result.isSuccessful()) {
            return Optional.of(result.getValue());
        } else {
            return Optional.empty();
        }
    }
}

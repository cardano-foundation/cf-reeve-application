package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.util.WithExtraIds;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class L1TransactionCreator {

    private final BackendService backendService;

    private final MetadataSerialiser metadataSerialiser;

    private final BlockchainDataChainTipService blockchainDataChainTipService;

    private final Account organiserAccount;

    @Value("${l1.transaction.metadata.label:22222}")
    private int metadataLabel;

    public Either<Problem, List<WithExtraIds<byte[]>>> createTransactions(List<TransactionLineEntity> txLines) {
        val chainTipE = blockchainDataChainTipService
                .latestChainTip();

        return chainTipE.map(chainTip -> {

            val txLineIdToMetadataMapMapping =
                    metadataSerialiser.serialiseToMetadataMap(txLines, chainTip.absoluteSlot());

            return txLineIdToMetadataMapMapping.stream()
                    .map(item -> {
                        val metadata = MetadataBuilder.createMetadata();
                        metadata.put(metadataLabel, item.getCompanion());

                        val txBytes = serialiseTransaction(metadata);

                        return new WithExtraIds<>(item.getIds(), txBytes);
                    })
                    .toList();
        });
    }

    @SneakyThrows
    protected byte[] serialiseTransaction(Metadata metadata) {
        val quickTxBuilder = new QuickTxBuilder(backendService);

        val tx = new Tx()
                .payToAddress(organiserAccount.baseAddress(), Amount.ada(2.0))
                .attachMetadata(metadata)
                .from(organiserAccount.baseAddress());

        return quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(organiserAccount))
                .buildAndSign()
                .serialize();
    }


}
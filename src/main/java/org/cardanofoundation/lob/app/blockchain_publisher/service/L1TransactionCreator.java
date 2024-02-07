package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.google.common.collect.Sets;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainTransactionWithLines;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.collections4.iterators.PeekingIterator.peekingIterator;

@Service
@Slf4j
@RequiredArgsConstructor
public class L1TransactionCreator {

    private static final int CARDANO_MAX_TRANSACTION_SIZE_BYTES = 16384;

    private final BackendService backendService;

    private final MetadataSerialiser metadataSerialiser;

    private final BlockchainDataChainTipService blockchainDataChainTipService;

    private final Account organiserAccount;

    @Value("${l1.transaction.metadata.label:22222}")
    private int metadataLabel;

    public Either<Problem, Optional<BlockchainTransactionWithLines>> pullBlockchainTransaction(String organisationId,
                                                                                               List<TransactionLineEntity> txLines) {
        val chainTipE = blockchainDataChainTipService.latestChainTip();

        return chainTipE.map(chainTip -> createTransaction(organisationId, txLines, chainTip.absoluteSlot()));
    }

    private Optional<BlockchainTransactionWithLines> createTransaction(String organisationId,
                                                                       List<TransactionLineEntity> transactionLines,
                                                                       long creationSlot) {
        log.info("Splitting {} transaction lines into blockchain transactions", transactionLines.size());

        val transactionsBatch = new ArrayList<TransactionLineEntity>();

        for (var it = peekingIterator(transactionLines.iterator()); it.hasNext();) {
            val txLine = it.next();

            transactionsBatch.add(txLine);

            val txBytes = serialiseTransactionChunk(transactionsBatch, creationSlot);

            val transactionLinePeek = it.peek();
            if (transactionLinePeek == null) { // next one is last element
                continue;
            }
            val newChunkTxBytes = serialiseTransactionChunk(Stream.concat(transactionsBatch.stream(), Stream.of(transactionLinePeek)).toList(), creationSlot);

            if (newChunkTxBytes.length >= CARDANO_MAX_TRANSACTION_SIZE_BYTES) {
                log.info("Blockchain transaction created, id:{}", TransactionUtil.getTxHash(txBytes));

                log.info("Physical transaction size:{}", txBytes.length);

                final var remaining = calculateRemainingTransactionLines(transactionLines, transactionsBatch);

                return Optional.of(new BlockchainTransactionWithLines(organisationId, transactionsBatch, remaining, txBytes));
            }
        }

        // if there are any left overs
        if (!transactionsBatch.isEmpty()) {
            log.info("Last batch size: {}", transactionsBatch.size());

            val txBytes = serialiseTransactionChunk(transactionsBatch, creationSlot);

            log.info("Transaction size: {}", txBytes.length);

            final var remaining = calculateRemainingTransactionLines(transactionLines, transactionsBatch);

            return Optional.of(new BlockchainTransactionWithLines(organisationId, transactionsBatch, remaining, txBytes));
        }

        return Optional.empty();
    }

    private static List<TransactionLineEntity> calculateRemainingTransactionLines(
            List<TransactionLineEntity> transactionLines,
            List<TransactionLineEntity> transactionsBatch) {

        return Sets.difference(new HashSet<>(transactionLines), new HashSet<>(transactionsBatch)).stream().toList();
    }

    private byte[] serialiseTransactionChunk(List<TransactionLineEntity> transactionsBatch,
                                             long creationSlot) {
        val txLineIdToMetadataMapMapping =
                metadataSerialiser.serialiseToMetadataMap(transactionsBatch, creationSlot);

        val metadata = MetadataBuilder.createMetadata();
        metadata.put(metadataLabel, txLineIdToMetadataMapMapping.getCompanion());

        return serialiseTransaction(metadata);
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

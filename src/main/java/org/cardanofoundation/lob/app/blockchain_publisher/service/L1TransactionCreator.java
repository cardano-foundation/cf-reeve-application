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
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.util.WithExtraIds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

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

    public Either<Problem, List<WithExtraIds<byte[]>>> createTransactions(List<TransactionLineEntity> txLines) {
        val chainTipE = blockchainDataChainTipService.latestChainTip();

        return chainTipE.map(chainTip -> splitIntoBlockchainTransactions(txLines, chainTip.absoluteSlot()));
    }

    private List<WithExtraIds<byte[]>> splitIntoBlockchainTransactions(List<TransactionLineEntity> transactionLines,
                                                                       long creationSlot) {
        log.info("Splitting {} transaction lines into blockchain transactions", transactionLines.size());

        val transactionsBatch = new ArrayList<TransactionLineEntity>();

        val blockchainTransactions = new ArrayList<WithExtraIds<byte[]>>();

        for (var it = PeekingIterator.peekingIterator(transactionLines.iterator()); it.hasNext();) {
            val txLine = it.next();

            transactionsBatch.add(txLine);

            val txBytes = serialiseTransactionChunk(transactionsBatch, creationSlot);


            val extraIds = new WithExtraIds<>(transactionsBatch.stream()
                    .map(TransactionLineEntity::getId)
                    .collect(toSet()), txBytes);

            val transactionLinePeek = it.peek();
            if (transactionLinePeek == null) { // next one is last element
                continue;
            }
            val newChunkTxBytes = serialiseTransactionChunk(Stream.concat(transactionsBatch.stream(), Stream.of(transactionLinePeek)).toList(), creationSlot);

            if (newChunkTxBytes.length >= CARDANO_MAX_TRANSACTION_SIZE_BYTES) {
                log.info("Blockchain transaction created, id:{}", TransactionUtil.getTxHash(extraIds.getCompanion()));

                log.info("Physical transaction size:{}", txBytes.length);

                blockchainTransactions.add(extraIds);
                transactionsBatch.clear();
            }
        }

        // if there are any left overs
        if (!transactionsBatch.isEmpty()) {
            log.info("Last batch size: {}", transactionsBatch.size());

            val txBytes = serialiseTransactionChunk(transactionsBatch, creationSlot);

            log.info("Transaction size: {}", txBytes.length);

            val extraIds = new WithExtraIds<>(transactionsBatch.stream()
                    .map(TransactionLineEntity::getId)
                    .collect(toSet()), txBytes);

            blockchainTransactions.add(extraIds);
            transactionsBatch.clear();
        }

        log.info("Created {} blockchain transactions", blockchainTransactions.size());

        return blockchainTransactions;
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

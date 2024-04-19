package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchAssocRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionItemRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionBatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.DISPATCHED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.NOT_DISPATCHED;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbSynchronisationServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionItemRepository transactionItemRepository;

    @Mock
    private TransactionBatchAssocRepository transactionBatchAssocRepository;

    @Mock
    private TransactionBatchService transactionBatchService;

    @InjectMocks
    private DbSynchronisationService service;

    @Test
    void shouldDoNothingWithEmptyTransactions() {
        val organisationTransactions = new OrganisationTransactions("org1", Set.of());

        service.synchroniseAndFlushToDb("batch1", organisationTransactions, Optional.of(0), new ProcessorFlags(false));
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transactionBatchService);
        verifyNoInteractions(transactionItemRepository);
    }

    @Test
    void shouldProcessReprocessFlag() {
        val batchId = "batch1";
        val txId = "tx1";

        val tx1 = new TransactionEntity();
        tx1.setId(txId);
        tx1.setTransactionInternalNumber("txn123");
        tx1.setTransactionApproved(true);
        tx1.setLedgerDispatchApproved(true);
        tx1.setLedgerDispatchStatus(DISPATCHED);

        val txs = Set.of(tx1);
        val transactions = new OrganisationTransactions("org1", txs);

        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer((Answer<TransactionEntity>) invocation -> (TransactionEntity) invocation.getArgument(0));
        service.synchroniseAndFlushToDb(batchId, transactions, Optional.of(1), new ProcessorFlags(true));

        verify(transactionRepository).save(eq(tx1));
        verify(transactionBatchAssocRepository).saveAll(any(Set.class));
    }

    @Test
    void shouldNotUpdateDispatchedTransactions() {
        val batchId = "batch1";
        val tx1 = new TransactionEntity();
        tx1.setId("tx1");
        tx1.setTransactionInternalNumber("txn123");
        tx1.setTransactionApproved(true);
        tx1.setLedgerDispatchApproved(true);
        tx1.setLedgerDispatchStatus(DISPATCHED);

        val txs = Set.of(tx1);
        val transactions = new OrganisationTransactions("org1", txs);

        when(transactionRepository.findAllById(eq(Set.of("tx1")))).thenReturn(List.of(tx1));

        service.synchroniseAndFlushToDb(batchId, transactions, Optional.of(1), new ProcessorFlags(false));

        verify(transactionRepository, never()).save(any());
        verify(transactionItemRepository, never()).save(any());
    }

    @Test
    void shouldStoreNewTransactions() {
        val batchId = "batch1";
        val tx1Id = "3112ec27094335dd858948b3086817d7e290586d235c529be21f03ba5d583503";

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(tx1Id, "0"));
        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(tx1Id, "1"));

        val items = new LinkedHashSet<TransactionItemEntity>();
        items.add(txItem1);

        val tx1 = new TransactionEntity();
        tx1.setId(tx1Id);
        tx1.setItems(items);

        val txs = Set.of(tx1);
        val transactions = new OrganisationTransactions("org1", txs);

        when(transactionRepository.findAllById(any())).thenReturn(List.of());
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer((Answer<TransactionEntity>) invocation -> (TransactionEntity) invocation.getArgument(0));

        service.synchroniseAndFlushToDb(batchId, transactions, Optional.of(txs.size()), new ProcessorFlags(false));

        verify(transactionRepository).save(eq(tx1));
        verify(transactionItemRepository).saveAll(eq(items));
    }

    @Test
    void shouldHandleMixedTransactions() {
        val dispatchedTx = new TransactionEntity();
        dispatchedTx.setId("tx1");
        dispatchedTx.setTransactionApproved(true);
        dispatchedTx.setLedgerDispatchApproved(true);
        dispatchedTx.setLedgerDispatchStatus(DISPATCHED);

        val notDispatchedTx = new TransactionEntity();
        notDispatchedTx.setId("tx2");
        dispatchedTx.setTransactionApproved(true);
        notDispatchedTx.setLedgerDispatchApproved(false);
        notDispatchedTx.setLedgerDispatchStatus(NOT_DISPATCHED);

        val txs = Set.of(dispatchedTx, notDispatchedTx);
        val mixedTransactions = new OrganisationTransactions("org1", txs);
        when(transactionRepository.findAllById(any())).thenReturn(List.of(dispatchedTx));

        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer((Answer<TransactionEntity>) invocation -> (TransactionEntity) invocation.getArgument(0));

        service.synchroniseAndFlushToDb("batch1", mixedTransactions, Optional.of(2), new ProcessorFlags(false));

        verify(transactionRepository, never()).save(dispatchedTx);
        verify(transactionRepository).save(notDispatchedTx);
        verify(transactionItemRepository).saveAll(any());
    }

}

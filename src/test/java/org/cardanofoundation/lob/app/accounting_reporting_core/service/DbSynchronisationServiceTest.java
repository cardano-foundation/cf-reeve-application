package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchAssocRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionItemRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    private OrganisationTransactions emptyTransactions;

    private ProcessorFlags flags;

    private TransactionEntity tx1;

    @BeforeEach
    void setUp() {
        emptyTransactions = new OrganisationTransactions("org1", new HashSet<>());
        this.tx1 = new TransactionEntity();
        tx1.setId("tx1");

        flags = new ProcessorFlags(false);
    }

    @Test
    void shouldDoNothingWithEmptyTransactions() {
        // Q: write tests for this scenario
        service.synchroniseAndFlushToDb("batch1", emptyTransactions, Optional.of(1), flags);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transactionBatchService);
        verifyNoInteractions(transactionItemRepository);
    }

    @Test
    void shouldProcessReprocessFlag() {
        val transactions = new OrganisationTransactions("org1", Set.of(tx1));
        flags = new ProcessorFlags(true);

        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer((Answer<TransactionEntity>) invocation -> (TransactionEntity) invocation.getArgument(0));
        service.synchroniseAndFlushToDb("batch1", transactions, Optional.of(1), flags);

        verify(transactionRepository).save(eq(tx1));
        verify(transactionBatchAssocRepository).saveAll(any(Set.class));
    }

//    @Test
//    void shouldUpdateChangedAndNotDispatchedTransactions() {
//        // Arrange: Transaction setup as not dispatched and expected to be saved
//        TransactionEntity transactionEntity = new TransactionEntity();
//        transactionEntity.setId("tx1");
//        transactionEntity.setTransactionInternalNumber("txn123");
//        transactionEntity.setLedgerDispatchStatus(NOT_DISPATCHED);
//
//        // Mock: Return the input transaction on save to simulate JPA's save behavior
//        when(transactionRepository.findAllById(any(Set.class))).thenReturn(List.of(transactionEntity));
//        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Act: Call the method under test
//        service.synchroniseAndFlushToDb("batch1", transactions, Optional.of(1), flags);
//
//        // Assert: Ensure all relevant methods are called
//        verify(transactionRepository).save(any(TransactionEntity.class));
//        verify(transactionItemRepository).saveAll(any());
//        verify(transactionBatchAssocRepository).saveAll(any());
//        verify(transactionBatchService).updateTransactionBatchStatusAndStats("batch1", Optional.of(1));
//    }
//
//    @Test
//    void shouldNotUpdateChangedAndDispatchedTransactions() {
//        TransactionEntity tx = new TransactionEntity();
//        tx.setId("tx1");
//        tx.setLedgerDispatchStatus(DISPATCHED);
//
//        when(transactionRepository.findAllById(any())).thenReturn(List.of(tx));
//        service.synchroniseAndFlushToDb("batch1", transactions, Optional.of(1), flags);
//        verify(transactionRepository, never()).saveAll(any());
//        verify(transactionItemRepository, never()).saveAll(any());
//    }
//
//    @Test
//    void shouldHandleMixedTransactions() {
//        TransactionEntity dispatchedTx = new TransactionEntity();
//        dispatchedTx.setId("tx1");
//        dispatchedTx.setLedgerDispatchStatus(DISPATCHED);
//
//        TransactionEntity notDispatchedTx = new TransactionEntity();
//        notDispatchedTx.setId("tx2");
//        notDispatchedTx.setLedgerDispatchStatus(NOT_DISPATCHED);
//
//        OrganisationTransactions mixedTransactions = new OrganisationTransactions("org1", Set.of(dispatchedTx, notDispatchedTx));
//        when(transactionRepository.findAllById(any())).thenReturn(List.of(dispatchedTx));
//
//        service.synchroniseAndFlushToDb("batch1", mixedTransactions, Optional.of(2), flags);
//
//        verify(transactionRepository, never()).save(dispatchedTx);
//        verify(transactionRepository).save(notDispatchedTx);
//        verify(transactionItemRepository).saveAll(any());
//    }

}
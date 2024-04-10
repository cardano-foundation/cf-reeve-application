package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.TX_CANNOT_BE_ALTERED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.WARN;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbSyncProcessorPipelineTaskTest {

    @Mock
    private TransactionRepositoryGateway transactionRepositoryGateway;

    private DbSyncProcessorPipelineTask dbSyncProcessorPipelineTask;

    @BeforeEach
    void setUp() {
        dbSyncProcessorPipelineTask = new DbSyncProcessorPipelineTask(transactionRepositoryGateway);
    }

    @Test
    void testEmptyPassedTransactions() {
        val orgId = "org1";
        // Create empty passed transactions and an empty set for ignored transactions
        OrganisationTransactions passedTransactions = new OrganisationTransactions(orgId, new HashSet<>());
        OrganisationTransactions ignoredTransactions = new OrganisationTransactions(orgId, new HashSet<>());

        // No need to mock `findByAllId` as no transaction IDs will be queried

        // Execute
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, ignoredTransactions);

        // Assert
        assertThat(result.passedTransactions().transactions()).isEmpty(); // Expect no transactions to be processed
        assertThat(result.ignoredTransactions().transactions()).isEmpty(); // Expect no transactions to be ignored
        assertThat(result.getAllViolations()).isEmpty(); // Expect no violations to be generated

        // Verify that findByAllId was not called since there are no transactions to process
        verify(transactionRepositoryGateway, never()).findByAllId(anySet());
    }

    @Test
    void testNoTransactionsToProcess() {
        // Setup mock data and expected results
        val passedTransactions = new OrganisationTransactions("org1", new HashSet<>());
        val ignoredTransactions = new OrganisationTransactions("org1", new HashSet<>());

        // Execute
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, ignoredTransactions);

        // Assert
        assertThat(result.passedTransactions().transactions()).isEmpty();
        assertThat(result.ignoredTransactions().transactions()).isEmpty();
        assertThat(result.getAllViolations()).isEmpty();
    }

    @Test
    void testAllTransactionsDispatched() {
        val orgId = "org1";
        // Assume these transactions are both previously dispatched and unchanged
        val tx1 = Transaction.builder()
                .id("tx1")
                .internalTransactionNumber("internalTx1")
                .transactionApproved(true)
                .ledgerDispatchApproved(true)
                .build();
        val tx2 = Transaction.builder()
                .id("tx2")
                .internalTransactionNumber("internalTx2")
                .transactionApproved(true)
                .ledgerDispatchApproved(true)
                .build();

        val passedTransactions = new OrganisationTransactions(orgId, Set.of(tx1, tx2));
        val ignoredTransactions = new OrganisationTransactions(orgId, new HashSet<>());

        // Mocking the repository to return the transactions as both dispatched (via allApprovalsPassedForTransactionDispatch)
        // and unchanged (since they match the incoming transactions exactly)
        when(transactionRepositoryGateway.findByAllId(anySet())).thenReturn(Set.of(tx1, tx2));

        val result = dbSyncProcessorPipelineTask.run(passedTransactions, ignoredTransactions);

        // Since both transactions are mocked as dispatched and unchanged, they should be ignored
        assertThat(result.passedTransactions().transactions()).isEmpty();
        assertThat(result.ignoredTransactions().transactions()).containsExactlyInAnyOrderElementsOf(Set.of(tx1, tx2));
        assertThat(result.getAllViolations()).isEmpty();
    }

    @Test
    void testNoTransactionsDispatched() {
        val orgId = "org1";
        val internalTx1 = "internalTx1";
        val internalTx2 = "internalTx2";

        val tx1Id = Transaction.id(orgId, internalTx1);
        val tx2Id = Transaction.id(orgId, internalTx2);

        val tx1 = Transaction.builder()
                .id(tx1Id)
                .internalTransactionNumber(internalTx1)
                .items(Set.of(
                        TransactionItem.builder().id(TransactionItem.id(tx1Id, "0")).build(),
                        TransactionItem.builder().id(TransactionItem.id(tx1Id, "1")).build()
                ))
                .build();

        val tx2 = Transaction.builder()
                .id(tx2Id)
                .internalTransactionNumber(internalTx2)
                .items(
                        Set.of(
                                TransactionItem.builder().id(TransactionItem.id(tx2Id, "0")).build()
                        ))
                .build();

        val passedTransactions = new OrganisationTransactions(orgId, Set.of(tx1, tx2));
        val ignoredTransactions = new OrganisationTransactions(orgId, new HashSet<>());

        when(transactionRepositoryGateway.findByAllId(anySet()))
                .thenReturn(new HashSet<>());

        // When
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, ignoredTransactions);

        // Then
        assertThat(result.passedTransactions().transactions()).containsExactlyInAnyOrder(tx1, tx2);
        assertThat(result.ignoredTransactions().transactions()).isEmpty();
        assertThat(result.getAllViolations()).isEmpty();
    }

    @Test
    void testMixOfDispatchedAndNonDispatchedTransactions() {
        val orgId = "org1";
        val internalTx1 = "internalTx1";
        val internalTx2 = "internalTx2";

        val tx1Id = Transaction.id(orgId, internalTx1);
        val tx2Id = Transaction.id(orgId, internalTx2);

        // Simulate tx1 as dispatched and unchanged, tx2 as new or changed and not dispatched
        val tx1 = Transaction.builder()
                .id(tx1Id)
                .internalTransactionNumber(internalTx1)
                .transactionApproved(true)
                .ledgerDispatchApproved(true)
                .items(Set.of(
                        TransactionItem.builder().id(TransactionItem.id(tx1Id, "0")).build(),
                        TransactionItem.builder().id(TransactionItem.id(tx1Id, "1")).build()
                ))
                .build();

        val tx2 = Transaction.builder()
                .id(tx2Id)
                .internalTransactionNumber(internalTx2)
                .transactionApproved(true)
                .ledgerDispatchApproved(false)
                .items(Set.of(
                        TransactionItem.builder().id(TransactionItem.id(tx2Id, "0")).build()
                ))
                .build();

        val passedTransactions = new OrganisationTransactions(orgId, Set.of(tx1, tx2));
        val ignoredTransactions = new OrganisationTransactions(orgId, new HashSet<>());

        // Mocking: Assuming findByAllId should return only tx1 as it simulates being present in the database and dispatched
        when(transactionRepositoryGateway.findByAllId(anySet())).thenReturn(Set.of(tx1));

        // When
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, ignoredTransactions);

        // Then: Expecting tx2 to be processed because it's not dispatched; tx1 ignored because it's dispatched

        assertThat(result.passedTransactions().transactions()).containsExactly(tx2);
        assertThat(result.ignoredTransactions().transactions()).containsExactly(tx1);

        assertThat(result.violations()).isEmpty();
    }

    @Test
    void testProcessingWithPreExistingViolations() {
        // Initialize basic setup for organisation ID and internal transaction numbers
        val orgId = "org1";
        val internalTx1 = "internalTx1";
        val internalTx2 = "internalTx2";

        // Pre-existing violation for a transaction that is not part of this test's transaction set
        val preExistingViolation = Violation.create(WARN, ERP, TX_CANNOT_BE_ALTERED, DbSyncProcessorPipelineTask.class.getName(), Map.of("note", "This is a pre-existing violation."));

        // Create transaction IDs using a hypothetical static method on the Transaction class
        val tx1Id = Transaction.id(orgId, internalTx1);
        val tx2Id = Transaction.id(orgId, internalTx2);

        // Build transactions using a hypothetical builder pattern
        val tx1 = Transaction.builder()
                .id(tx1Id)
                .internalTransactionNumber(internalTx1)
                .items(Set.of(
                        TransactionItem.builder().id(TransactionItem.id(tx1Id, "0")).build(),
                        TransactionItem.builder().id(TransactionItem.id(tx1Id, "1")).build()
                ))
                .violations(Set.of(preExistingViolation))
                .build();

        val tx2 = Transaction.builder()
                .id(tx2Id)
                .internalTransactionNumber(internalTx2)
                .items(Set.of(
                        TransactionItem.builder().id(TransactionItem.id(tx2Id, "0")).build()
                ))
                .build();

        // Assume both transactions have not been dispatched yet
        val passedTransactions = new OrganisationTransactions(orgId, Set.of(tx1, tx2));
        val ignoredTransactions = new OrganisationTransactions(orgId, new HashSet<>());

        // Setup mock behavior for the transactionRepositoryGateway
        when(transactionRepositoryGateway.findByAllId(anySet()))
                .thenReturn(new HashSet<>()); // No transactions have been dispatched

        // Execute the task
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, ignoredTransactions);

        // Assert that the result contains the pre-existing violation and no new violations
        assertThat(result.passedTransactions().transactions()).containsExactlyInAnyOrderElementsOf(Set.of(tx1, tx2));
        assertThat(result.ignoredTransactions().transactions()).isEmpty();
        assertThat(result.violations()).containsExactlyInAnyOrder(preExistingViolation); // Only the pre-existing violation should be present

        // Verify interactions with the mock
        verify(transactionRepositoryGateway).findByAllId(anySet());
    }

    @Test
    void testTransactionsInAllCategories() {
        val orgId = "org1";

        // New incoming transaction that has changed (with a later date)
        val txNewChanged1 = Transaction.builder()
                .id("txChanged")
                .internalTransactionNumber("changed1")
                .transactionApproved(false)
                .ledgerDispatchApproved(false)
                .entryDate(LocalDate.of(2021, 2, 1)) // Later date to simulate the change
                .build();

        // Old version of the changed transaction from the database (with an earlier date)
        val txNewChanged2 = Transaction.builder()
                .id("txChanged")
                .internalTransactionNumber("changed2")
                .transactionApproved(false)
                .ledgerDispatchApproved(false)
                .entryDate(LocalDate.of(2021, 1, 1)) // Earlier date to represent the unchanged version
                .build();

        // Other transactions setup as before
        val txNewUnchanged = Transaction.builder()
                .id("txNewUnchanged")
                .internalTransactionNumber("newUnchanged")
                .transactionApproved(false)
                .ledgerDispatchApproved(false)
                .entryDate(LocalDate.of(2021, 1, 1))
                .build();

        val txDispatchedUnchanged = Transaction.builder()
                .id("txDispatchedUnchanged")
                .internalTransactionNumber("dispatchedUnchanged")
                .transactionApproved(true)
                .ledgerDispatchApproved(true)
                .entryDate(LocalDate.of(2021, 1, 1))
                .build();

        val passedTransactions = new OrganisationTransactions(orgId, Set.of(txNewChanged1, txNewUnchanged, txDispatchedUnchanged));

        // Simulating database transactions: includes the old version of the changed transaction and the unchanged ones
        when(transactionRepositoryGateway.findByAllId(anySet())).thenReturn(Set.of(txNewChanged2, txNewUnchanged, txDispatchedUnchanged));

        // Execute
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, new OrganisationTransactions(orgId, new HashSet<>()));

        // Asserts
        assertThat(result.passedTransactions().transactions()).containsExactlyInAnyOrder(txNewChanged1); // Only the changed transaction is processed
        assertThat(result.ignoredTransactions().transactions()).containsExactlyInAnyOrder(txNewUnchanged, txDispatchedUnchanged); // Unchanged transactions are ignored
        assertThat(result.violations()).isEmpty();

        // Verify
        verify(transactionRepositoryGateway).findByAllId(anySet());
    }

    @Test
    void testViolationsForIgnoredTransactions() {
        val orgId = "org2";
        val internalTx1 = "internalIgnoredTx1";
        val internalTx2 = "internalProcessedTx2";

        // Simulate tx1 as a dispatched and unchanged transaction
        val tx1 = Transaction.builder()
                .id("tx1Id")
                .internalTransactionNumber(internalTx1)
                .transactionApproved(true)
                .ledgerDispatchApproved(true)
                .entryDate(LocalDate.of(2021, 1, 1))
                .build();

        // Simulate tx2 as new or changed and not yet dispatched
        val tx2 = Transaction.builder()
                .id("tx2Id")
                .internalTransactionNumber(internalTx2)
                .transactionApproved(false)
                .ledgerDispatchApproved(false)
                .entryDate(LocalDate.of(2021, 2, 1)) // Later date to imply a change or new entry
                .build();

        val passedTransactions = new OrganisationTransactions(orgId, Set.of(tx1, tx2));

        // Mock the database return to simulate tx1 being unchanged and previously dispatched,
        // while tx2 is not found in the database (implying it's new or has changed).
        when(transactionRepositoryGateway.findByAllId(anySet()))
                .thenReturn(Set.of(tx1)); // Only tx1 is found in the DB

        // Execute
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, new OrganisationTransactions(orgId, new HashSet<>()));

        // Assertions
        assertThat(result.passedTransactions().transactions()).containsExactly(tx2); // tx2 is new/changed and processed
        assertThat(result.ignoredTransactions().transactions()).containsExactly(tx1); // tx1 is ignored because it's dispatched/unchanged
        assertThat(result.violations()).isEmpty(); // Assuming this test setup doesn't generate violations

        // Verify interactions
        verify(transactionRepositoryGateway).findByAllId(anySet());
    }

    @Test
    void testTransactionWithMultipleChanges() {
        val orgId = "org1";
        val internalTxNumber = "internalTx123";
        val batchId1 = "batch1";
        val batchId2 = "batch2";

        // Original transaction version in the database
        val txOriginal = Transaction.builder()
                .id(Transaction.id(orgId, internalTxNumber))
                .internalTransactionNumber(internalTxNumber)
                .batchId(batchId1)
                .entryDate(LocalDate.of(2021, 1, 1))
                .transactionType(TransactionType.Transfer)
                .organisation(Organisation.builder().id("org1").build())
                .ledgerDispatchStatus(LedgerDispatchStatus.NOT_DISPATCHED)
                .fxRate(new BigDecimal("1.00"))
                .accountingPeriod(YearMonth.of(2021, 1))
                .build();

        // First update to the transaction
        val txUpdatedOnce = txOriginal.toBuilder()
                .entryDate(LocalDate.of(2021, 2, 1))
                .fxRate(new BigDecimal("1.05"))
                .batchId(batchId2)
                .build();

        // Second and final update to the transaction
        val txUpdatedTwice = txUpdatedOnce.toBuilder()
                .entryDate(LocalDate.of(2021, 3, 1))
                .fxRate(new BigDecimal("1.10"))
                .build();

        // Mock the repository to return the original version, simulating database state
        when(transactionRepositoryGateway.findByAllId(anySet())).thenReturn(Set.of(txOriginal));

        val passedTransactions = new OrganisationTransactions(orgId, Set.of(txUpdatedTwice));

        // Execute
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, new OrganisationTransactions(orgId, new HashSet<>()));

        // Assertions
        assertThat(result.passedTransactions().transactions()).containsExactly(txUpdatedTwice);
        assertThat(result.ignoredTransactions().transactions()).isEmpty();
        assertThat(result.violations()).isEmpty();

        // Verify interactions
        verify(transactionRepositoryGateway).findByAllId(anySet());
    }

    @Test
    void testUnapprovedChangedTransactionsProcessed() {
        val orgId = "org1";

        // Define transactions with different approval and change statuses
        val txChangedNotApproved = Transaction.builder()
                .id("txChangedNotApproved")
                .internalTransactionNumber("TX1001")
                .entryDate(LocalDate.of(2022, 1, 15)) // This date represents a change
                .transactionApproved(false)
                .ledgerDispatchApproved(false)
                .build();

        // Original (database) state of the changed transaction (to simulate it being unchanged in the DB)
        val txChangedNotApprovedOriginal = txChangedNotApproved.toBuilder()
                .entryDate(LocalDate.of(2022, 1, 10)) // Original date before change
                .build();

        val passedTransactions = new OrganisationTransactions(orgId, Set.of(txChangedNotApproved));

        // Simulate the original transaction state as found in the database
        when(transactionRepositoryGateway.findByAllId(Set.of(txChangedNotApproved.getId())))
                .thenReturn(Set.of(txChangedNotApprovedOriginal));

        // Execute the task with the simulated changed transaction
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, new OrganisationTransactions(orgId, new HashSet<>()));

        // Assertions
        assertThat(result.passedTransactions().transactions()).containsExactly(txChangedNotApproved); // The changed transaction should be processed
        assertThat(result.ignoredTransactions().transactions()).isEmpty(); // No transactions should be ignored in this test case
        assertThat(result.violations()).isEmpty(); // Ensure no violations are generated for unapproved but changed transactions

        // Verify
        verify(transactionRepositoryGateway).findByAllId(anySet());
    }

    @Test
    void testViolationsForChangedTransactions() {
        val orgId = "org2";

        // Original transaction as stored in the database (unchanged)
        val tx1Original = Transaction.builder()
                .id("tx1Id")
                .internalTransactionNumber("internalTx1")
                .transactionApproved(true)
                .ledgerDispatchApproved(true)
                .entryDate(LocalDate.of(2021, 1, 1))
                .build();

        // New incoming version of tx1 with changes
        val tx1Changed = Transaction.builder()
                .id("tx1Id") // Same ID to indicate it's the same transaction
                .internalTransactionNumber("internalTx1")
                .transactionApproved(true) // Assume it's still approved
                .ledgerDispatchApproved(true) // Assume it's still approved for ledger dispatch
                .entryDate(LocalDate.of(2021, 1, 2)) // Change in the entry date
                .violations(Set.of(Violation.create(WARN, ERP, TX_CANNOT_BE_ALTERED, DbSyncProcessorPipelineTask.class.getName(), Map.of("transactionNumber", "internalTx1"))))
                .build();

        // Simulate tx2 as new or changed and not yet dispatched (for completeness)
        val tx2 = Transaction.builder()
                .id("tx2Id")
                .internalTransactionNumber("internalProcessedTx2")
                .transactionApproved(false)
                .ledgerDispatchApproved(false)
                .entryDate(LocalDate.of(2021, 2, 1))
                .build();

        val passedTransactions = new OrganisationTransactions(orgId, Set.of(tx1Changed, tx2));

        // Mock the database to return the original version of tx1, simulating that it's unchanged and previously dispatched
        when(transactionRepositoryGateway.findByAllId(anySet())).thenReturn(Set.of(tx1Original));

        // Execute
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, new OrganisationTransactions(orgId, new HashSet<>()));

        // Assertions
        assertThat(result.passedTransactions().transactions()).containsExactlyInAnyOrder(tx1Changed, tx2); // Both tx1Changed and tx2 are processed
        assertThat(result.ignoredTransactions().transactions()).isEmpty(); // No transactions should be ignored since tx1 is considered changed
        assertThat(result.violations()).hasSize(1);

        // Verify interactions
        verify(transactionRepositoryGateway).findByAllId(anySet());
    }

    @Test
    void testReProcessingPreviouslyFailedTransactions() {
        val orgId = "org1";
        // Original transaction state in the database marked as FAILED
        val txFailedOriginal = Transaction.builder()
                .id("tx1")
                .internalTransactionNumber("internalTx1")
                .validationStatus(ValidationStatus.FAILED)
                .transactionApproved(false)
                .ledgerDispatchApproved(false)
                .build();

        // Incoming version of the transaction marked as VALIDATED
        val txValidatedUpdated = Transaction.builder()
                .id("tx1") // Same ID to indicate it's the same transaction
                .internalTransactionNumber("internalTx1")
                .validationStatus(ValidationStatus.VALIDATED) // Updated validation status
                .transactionApproved(false)
                .ledgerDispatchApproved(false)
                .build();

        val passedTransactions = new OrganisationTransactions(orgId, Set.of(txValidatedUpdated));

        // Mock the database to return the original version of the transaction, simulating that it's previously failed
        when(transactionRepositoryGateway.findByAllId(Set.of(txFailedOriginal.getId()))).thenReturn(Set.of(txFailedOriginal));

        // Execute the task with the simulated updated transaction
        val result = dbSyncProcessorPipelineTask.run(passedTransactions, new OrganisationTransactions(orgId, new HashSet<>()));

        // Assertions
        assertThat(result.passedTransactions().transactions()).containsExactly(txValidatedUpdated); // The updated transaction should be re-processed
        assertThat(result.ignoredTransactions().transactions()).isEmpty(); // No transactions should be ignored in this specific case
        assertThat(result.violations()).isEmpty(); // Ensure no violations are generated for a transaction moving from FAILED to VALIDATED

        // Verify
        verify(transactionRepositoryGateway).findByAllId(anySet());
    }

}

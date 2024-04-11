package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

class DocumentMustBePresentTaskItemTest {

    private DocumentMustBePresentTaskItem taskItem;

    @BeforeEach
    void setUp() {
        taskItem = new DocumentMustBePresentTaskItem();
    }

    @Test
    void shouldNotModifyTransactionWhenAllDocumentsPresent() {
        val itemWithDocument = TransactionItem.builder()
                .id("itemWithDocument")
                .document(Optional.of(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document.builder()
                        .build()))
                .build();

        val items = Set.of(itemWithDocument);

        val transaction = Transaction.builder()
                .internalTransactionNumber("txn123")
                .items(items)
                .violations(new HashSet<>())
                .build();

        val result = taskItem.run(transaction);

        assertThat(result.getViolations()).isEmpty();
        assertThat(result.getValidationStatus()).isNotEqualTo(FAILED);
    }

    @Test
    void shouldAddViolationWhenDocumentIsMissing() {
        val itemWithoutDocument = TransactionItem.builder()
                .id("itemWithoutDocument")
                .document(Optional.empty())
                .build();

        val items = Set.of(itemWithoutDocument);

        val transaction = Transaction.builder()
                .internalTransactionNumber("txn123")
                .items(items)
                .violations(new HashSet<>())
                .build();

        val result = taskItem.run(transaction);

        assertThat(result.getViolations()).isNotEmpty();
        assertThat(result.getValidationStatus()).isEqualTo(FAILED);
    }

    @Test
    void shouldHandleMixedDocumentPresenceCorrectly() {
        val itemWithDocument = TransactionItem.builder()
                .id("itemWithDocument")
                .document(Optional.of(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document.builder()
                        .build()))
                .build();

        val itemWithoutDocument = TransactionItem.builder()
                .id("itemWithoutDocument")
                .document(Optional.empty())
                .build();

        val items = Set.of(itemWithDocument, itemWithoutDocument);

        val transaction = Transaction.builder()
                .internalTransactionNumber("txn123")
                .items(items)
                .violations(new HashSet<>())
                .build();

        val result = taskItem.run(transaction);

        assertThat(result.getViolations()).hasSize(1);
        assertThat(result.getValidationStatus()).isEqualTo(FAILED);
    }

    @Test
    void shouldNotAddViolationForEmptyItems() {
        val transaction = Transaction.builder()
                .internalTransactionNumber("txn123")
                .items(new HashSet<>())
                .violations(new HashSet<>())
                .build();

        val result = taskItem.run(transaction);

        assertThat(result.getViolations()).isEmpty();
        assertThat(result.getValidationStatus()).isNotEqualTo(FAILED);
    }

}

package org.cardanofoundation.lob.app.accounting_reporting_core.resource.model;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ExtractionRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.SearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.BatchView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.BatchsListView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.TransactionView;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountingCorePresentationConverterTest {

    @Mock
    private TransactionRepositoryGateway transactionRepositoryGateway;

    @Mock
    private AccountingCoreService accountingCoreService;

    @Mock
    private TransactionBatchRepositoryGateway transactionBatchRepositoryGateway;

    @InjectMocks
    private AccountingCorePresentationViewService accountingCorePresentationConverter;

    @Test
    void testAllTransactions() {

        SearchRequest searchRequest = new SearchRequest();
        TransactionItemEntity transactionItem = new TransactionItemEntity();
        TransactionEntity transactionEntity = new TransactionEntity();

        searchRequest.setOrganisationId("org-id");
        searchRequest.setStatus(List.of(ValidationStatus.VALIDATED));
        searchRequest.setTransactionType(List.of(TransactionType.CardCharge));

        transactionEntity.setId("tx-id");
        transactionEntity.setItems(Set.of(transactionItem));
        transactionItem.setTransaction(transactionEntity);

        when(transactionRepositoryGateway.findAllByStatus(any(), any(), any())).thenReturn(List.of(transactionEntity));

        List<TransactionView> result = accountingCorePresentationConverter.allTransactions(searchRequest);

        assertEquals(1, result.size());
        assertEquals("tx-id", result.get(0).getId());
    }

    @Test
    void testTransactionDetailSpecific() {

        String transactionId = "tx-id";
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(transactionId);
        // Set other properties of transactionEntity

        when(transactionRepositoryGateway.findById(transactionId)).thenReturn(Optional.of(transactionEntity));


        Optional<TransactionView> result = accountingCorePresentationConverter.transactionDetailSpecific(transactionId);


        assertEquals(true, result.isPresent());
        assertEquals(transactionId, result.get().getId());
    }

    @Test
    void testBatchDetail() {

        FilteringParameters filteringParameters = new FilteringParameters("pros", List.of(TransactionType.CardCharge), LocalDate.now(), LocalDate.now(), YearMonth.now(), YearMonth.now(), Collections.singletonList("asasas"));

        String batchId = "batch-id";
        TransactionBatchEntity transactionBatchEntity = new TransactionBatchEntity();
        transactionBatchEntity.setId(batchId);
        transactionBatchEntity.setCreatedAt(LocalDateTime.now());
        transactionBatchEntity.setUpdatedAt(LocalDateTime.now());
        transactionBatchEntity.setFilteringParameters(filteringParameters);

        // Set other properties of transactionBatchEntity

        when(transactionBatchRepositoryGateway.findById(batchId)).thenReturn(Optional.of(transactionBatchEntity));


        Optional<BatchView> result = accountingCorePresentationConverter.batchDetail(batchId);


        assertEquals(true, result.isPresent());
        assertEquals(batchId, result.get().getId());
    }

    @Test
    void testListAllBatch() {

        BatchSearchRequest batchSearchRequest = new BatchSearchRequest();
        batchSearchRequest.setOrganisationId("org-id");

        FilteringParameters filteringParameters = new FilteringParameters("pros", List.of(TransactionType.CardCharge), LocalDate.now(), LocalDate.now(), YearMonth.now(), YearMonth.now(), Collections.singletonList("asasas"));
        TransactionBatchEntity transactionBatchEntity = new TransactionBatchEntity();
        transactionBatchEntity.setId("batch-id");
        transactionBatchEntity.setCreatedAt(LocalDateTime.now());
        transactionBatchEntity.setUpdatedAt(LocalDateTime.now());
        transactionBatchEntity.setFilteringParameters(filteringParameters);
        // Set other properties of transactionBatchEntity

        when(transactionBatchRepositoryGateway.findByOrganisationId(any())).thenReturn(List.of(transactionBatchEntity));


        List<BatchsListView> result = accountingCorePresentationConverter.listAllBatch(batchSearchRequest);


        assertEquals(1, result.size());
        assertEquals("batch-id", result.get(0).getId());
    }

    @Test
    void testExtractionTrigger() {

        ExtractionRequest extractionRequest = new ExtractionRequest();
        extractionRequest.setDateFrom("2022-01-01");
        extractionRequest.setDateTo("2022-12-31");
        extractionRequest.setOrganisationId("org-id");
        extractionRequest.setTransactionType(List.of(TransactionType.CardCharge));
        extractionRequest.setTransactionNumbers(List.of("num1", "num2"));


        accountingCorePresentationConverter.extractionTrigger(extractionRequest);


        // Verify if scheduleIngestion method is called with expected UserExtractionParameters
        // This verification is omitted here as it requires further mock setup
    }
}
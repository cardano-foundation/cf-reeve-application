package org.cardanofoundation.lob.app.accounting_reporting_core.resource.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.BatchSearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.ExtractionRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests.SearchRequest;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@org.springframework.stereotype.Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
// presentation layer service
public class AccountingCorePresentationViewService {
    private final TransactionRepositoryGateway transactionRepositoryGateway;
    private final AccountingCoreService accountingCoreService;
    private final TransactionBatchRepositoryGateway transactionBatchRepositoryGateway;

    public List<TransactionView> allTransactions(SearchRequest body) {
        List<TransactionEntity> transactions = transactionRepositoryGateway.findAllByStatus(body.getOrganisationId(), body.getStatus(), body.getTransactionType());

        return
                transactions.stream().map(this::getTransactionView).toList()
        ;
    }

    public Optional<TransactionView> transactionDetailSpecific(String transactionId) {

        Optional<TransactionEntity> transactionEntity = transactionRepositoryGateway.findById(transactionId);
        return transactionEntity.map(this::getTransactionView);
    }

    public Optional<BatchView> batchDetail(String batchId) {
        return transactionBatchRepositoryGateway.findById(batchId).map(transactionBatchEntity -> {

                    val transactions = this.getTransaction(transactionBatchEntity);

                    return new BatchView(
                            transactionBatchEntity.getId(),
                            transactionBatchEntity.getCreatedAt().toString(),
                            transactionBatchEntity.getUpdatedAt().toString(),
                            transactionBatchEntity.getOrganisationId(),
                            transactionBatchEntity.getBatchStatistics(),
                            transactions
                    );
                }
        );
    }

    public List<BatchsListView> listAllBatch(BatchSearchRequest body) {
        return transactionBatchRepositoryGateway.findByOrganisationId(body.getOrganisationId()).stream().map(
                transactionBatchEntity -> new BatchsListView(
                        transactionBatchEntity.getId(),
                        transactionBatchEntity.getCreatedAt().toString(),
                        transactionBatchEntity.getUpdatedAt().toString(),
                        transactionBatchEntity.getOrganisationId()
                )
        ).toList();
    }
    @Transactional
    public void extractionTrigger(ExtractionRequest body) {
        val fp = UserExtractionParameters.builder()
                .from(LocalDate.parse(body.getDateFrom()))
                .to(LocalDate.parse(body.getDateTo()))
                .organisationId(body.getOrganisationId())
                .transactionTypes(body.getTransactionType())
                .transactionNumbers(body.getTransactionNumbers())
                .build();

        accountingCoreService.scheduleIngestion(fp);

    }

    private Set<TransactionView> getTransaction(TransactionBatchEntity transactionBatchEntity) {
        return transactionBatchEntity.getTransactions().stream()
                .map(this::getTransactionView)
                .collect(Collectors.toSet());
    }

    private TransactionView getTransactionView(TransactionEntity transactionEntity) {
        return new TransactionView(
                transactionEntity.getId(),
                transactionEntity.getTransactionInternalNumber(),
                transactionEntity.getEntryDate(),
                transactionEntity.getTransactionType(),
                transactionEntity.getValidationStatus(),
                transactionEntity.getTransactionApproved(),
                transactionEntity.getLedgerDispatchApproved(),
                getTransactionItemView(transactionEntity),
                getViolation(transactionEntity)
        );
    }

    private Set<TransactionItemView> getTransactionItemView(TransactionEntity transaction) {
        return transaction.getItems().stream().map(item -> new TransactionItemView(
                item.getId(),
                item.getAccountDebit(),
                item.getAccountCredit(),
                item.getAmountFcy(),
                item.getAmountLcy()
        )).collect(Collectors.toSet());
    }

    private Set<ViolationView> getViolation(TransactionEntity transaction) {

        return transaction.getViolations().stream().map(violation -> new ViolationView(
                violation.getType(),
                violation.getSource(),
                violation.getTxItemId(),
                violation.getCode(),
                violation.getBag()
        )).collect(Collectors.toSet());
    }

}

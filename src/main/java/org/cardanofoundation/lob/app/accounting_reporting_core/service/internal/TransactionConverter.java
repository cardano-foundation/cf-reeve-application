package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.CostCenter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Counterparty;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Document;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Project;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Vat;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service("accounting_reporting_core.TransactionConverter")
@Slf4j
@RequiredArgsConstructor
public class TransactionConverter {

    private final CoreCurrencyService coreCurrencyService;

    public FilteringParameters convert(SystemExtractionParameters systemExtractionParameters,
                                       UserExtractionParameters userExtractionParameters) {
        return FilteringParameters.builder()
                .organisationId(userExtractionParameters.getOrganisationId())
                .transactionTypes(userExtractionParameters.getTransactionTypes())
                .from(userExtractionParameters.getFrom())
                .to(userExtractionParameters.getTo())
                .accountingPeriodFrom(systemExtractionParameters.getAccountPeriodFrom())
                .accountingPeriodTo(systemExtractionParameters.getAccountPeriodTo())
                .transactionNumbers(userExtractionParameters.getTransactionNumbers())
                .build();
    }

    public Set<TransactionEntity> convertToDb(Set<Transaction> transactions) {
        return transactions.stream()
                .map(this::convert)
                .collect(Collectors.toSet());
    }

    public Set<Transaction> convertFromDb(List<TransactionEntity> transactionEntities) {
        return transactionEntities.stream()
                .map(this::convert)
                .collect(Collectors.toSet());
    }

    public Set<Transaction> convertFromDb(Set<TransactionEntity> transactionEntities) {
        return transactionEntities.stream()
                .map(this::convert)
                .collect(Collectors.toSet());
    }

    public TransactionEntity convert(Transaction transaction) {
        val transactionEntity = new TransactionEntity()
                .id(transaction.getId())
                .batchId(transaction.getBatchId())
                .transactionInternalNumber(transaction.getInternalTransactionNumber())
                .transactionType(transaction.getTransactionType())
                .entryDate(transaction.getEntryDate())
                .organisation(convertOrganisation(transaction))
                .fxRate(transaction.getFxRate())
                .validationStatus(transaction.getValidationStatus())
                .ledgerDispatchStatus(transaction.getLedgerDispatchStatus())
                .accountingPeriod(transaction.getAccountingPeriod())
                .transactionApproved(transaction.isTransactionApproved())
                .ledgerDispatchApproved(transaction.isLedgerDispatchApproved());

        val txItems = transaction.getItems()
                .stream()
                .map(txItemEntity -> {
                    return new TransactionItemEntity()
                            .id(txItemEntity.getId())
                            .transaction(transactionEntity)
                            .document(convert(txItemEntity.getDocument()))
                            .amountLcy(txItemEntity.getAmountLcy())
                            .costCenter(convertCostCenter(txItemEntity.getCostCenter()))
                            .amountFcy(txItemEntity.getAmountFcy())
                            .project(convertProject(txItemEntity.getProject()))
                            .accountCodeDebit(txItemEntity.getAccountCodeDebit().orElse(null))
                            .accountCodeRefDebit(txItemEntity.getAccountCodeEventRefDebit().orElse(null))
                            .accountCodeCredit(txItemEntity.getAccountCodeCredit().orElse(null))
                            .accountCodeRefCredit(txItemEntity.getAccountCodeEventRefCredit().orElse(null))
                            .accountNameDebit(txItemEntity.getAccountNameDebit().orElse(null))
                            .accountEventCode(txItemEntity.getAccountEventCode().orElse(null));

                })
                .collect(Collectors.toSet());

        val violations = transaction.getViolations().stream()
                .map(violation -> ViolationEntity.builder()
                        .id(new ViolationEntity.Id(transactionEntity.id(), violation.txItemId().orElse(""), violation.code()))
                        .transaction(transactionEntity)
                        .type(violation.type())
                        .source(violation.source())
                        .processorModule(violation.processorModule())
                        .bag(violation.bag())
                        .build())
                .collect(Collectors.toSet());

        transactionEntity.items(txItems);
        transactionEntity.violationEntities(violations);

        transactionEntity.createdAt(LocalDateTime.now());
        transactionEntity.updatedAt(LocalDateTime.now());
        transactionEntity.createdBy("system");
        transactionEntity.updatedBy("system");

        return transactionEntity;
    }

    private Project convertProject(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Project> project) {
        return project.map(p -> Project.builder()
                        .customerCode(p.getCustomerCode())
                        .build())
                .orElse(null);
    }

    private CostCenter convertCostCenter(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CostCenter> costCenter) {
        return costCenter.map(cc -> CostCenter.builder()
                        .customerCode(cc.getCustomerCode())
                        .externalCustomerCode(cc.getExternalCustomerCode().orElse(null))
                        .name(cc.getName().orElse(null))
                        .build())
                .orElse(null);
    }

    private static Organisation convertOrganisation(Transaction transaction) {
        return Organisation.builder()
                .id(transaction.getOrganisation().getId())
                .shortName(transaction.getOrganisation().getShortName().orElse(null))
                .currency(transaction.getOrganisation().getCurrency().map(c -> Currency.builder()
                        .id(c.getCoreCurrency().map(CoreCurrency::toExternalId).orElse(null))
                        .customerCode(c.getCustomerCode())
                        .build()).orElse(null))
                .build();
    }

    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Document convert(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document> docM) {
        return docM.map(doc -> Document.builder()
                        .num(doc.getNumber())

                        .currency(Currency.builder()
                                .customerCode(doc.getCurrency().getCustomerCode())
                                .id(doc.getCurrency().getCoreCurrency().map(CoreCurrency::toExternalId).orElse(null))
                                .build())
                        .vat(doc.getVat().map(vat -> Vat.builder()
                                .customerCode(vat.getCustomerCode())
                                .rate(vat.getRate().orElse(null))
                                .build()).orElse(null))

                        .counterparty(doc.getCounterparty().map(counterparty -> Counterparty.builder()
                                .customerCode(counterparty.getCustomerCode())
                                .type(counterparty.getType())
                                .name(counterparty.getName().orElse(null))
                                .build()).orElse(null)))

                .map(Document.DocumentBuilder::build)
                .orElse(null);
    }

    public Transaction convert(TransactionEntity transactionEntity) {
        val violations = transactionEntity.violationEntities()
                .stream()
                .map(violationEntity -> new org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation(
                        violationEntity.getType(),
                        violationEntity.getSource(),
                        violationEntity.getId().getTxItemId(),
                        violationEntity.getId().getCode(),
                        violationEntity.getProcessorModule(),
                        Map.of()
                ))
                //violationEntity.getBag()))
                .collect(Collectors.toSet());

        val items = transactionEntity.items()
                .stream()
                .map(txItemEntity -> TransactionItem.builder()
                        .id(txItemEntity.id())

                        .accountCodeDebit(txItemEntity.getAccountCodeDebit())
                        .accountCodeEventRefDebit(txItemEntity.getAccountCodeRefDebit())

                        .accountCodeCredit(txItemEntity.getAccountCodeCredit())
                        .accountCodeEventRefCredit(txItemEntity.getAccountCodeRefCredit())

                        .accountNameDebit(txItemEntity.getAccountNameDebit())

                        .accountEventCode(txItemEntity.getAccountEventCode())

                        .project(txItemEntity.getProject().map(project -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Project.builder()
                                .customerCode(project.getCustomerCode())
                                .build()))

                        .costCenter(txItemEntity.getCostCenter().map(costCenter -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CostCenter.builder()
                                .customerCode(costCenter.getCustomerCode())
                                .externalCustomerCode(costCenter.getExternalCustomerCode())
                                .name(costCenter.getName())
                                .build()))

                        .document(convert(txItemEntity.document()))

                        .amountFcy(txItemEntity.amountFcy())
                        .amountLcy(txItemEntity.amountLcy())

                        .build())
                .collect(Collectors.toSet());

        return Transaction.builder()
                .id(transactionEntity.id())
                .batchId(transactionEntity.batchId())
                .organisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation.builder()
                        .id(transactionEntity.organisation().getId())
                        .shortName(transactionEntity.organisation().getShortName())
                        .currency(transactionEntity.organisation().getCurrency().map(c -> {
                            return org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.builder()
                                    .customerCode(c.getCustomerCode())
                                    .coreCurrency(coreCurrencyService.findByCurrencyId(c.getId().orElseThrow()))
                                    .build();
                        }))
                        .build())
                .entryDate(transactionEntity.entryDate())
                .validationStatus(transactionEntity.validationStatus())
                .transactionType(transactionEntity.transactionType())
                .internalTransactionNumber(transactionEntity.transactionInternalNumber())
                .fxRate(transactionEntity.fxRate())

                .transactionApproved(transactionEntity.transactionApproved())
                .ledgerDispatchStatus(transactionEntity.ledgerDispatchStatus())
                .ledgerDispatchApproved(transactionEntity.ledgerDispatchApproved())
                .accountingPeriod(transactionEntity.accountingPeriod())
                .items(items)
                .violations(violations)
                .build();
    }

    private Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document> convert(@Nullable Document doc) {
        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document.builder()
                .number(doc.getNum())
                .currency(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.builder()
                        .customerCode(doc.getCurrency().getCustomerCode())
                        .coreCurrency(doc.getCurrency().getId().flatMap(coreCurrencyService::findByCurrencyId))
                        .build()
                )
                .vat(doc.getVat().map(vat -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Vat.builder()
                        .customerCode(vat.getCustomerCode())
                        .rate(vat.getRate())
                        .build()))

                .counterparty(doc.getCounterparty().map(counterparty -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.builder()
                        .customerCode(counterparty.getCustomerCode())
                        .name(counterparty.getName())
                        .build()))

                .build());
    }

}

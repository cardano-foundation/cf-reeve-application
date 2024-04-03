package org.cardanofoundation.lob.app.netsuite_adapter.service;

import io.vavr.control.Either;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.FinancialPeriodSource;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.TransactionsWithViolations;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.TxLine;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.Violation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type.VENDOR;
import static org.cardanofoundation.lob.app.netsuite_adapter.domain.core.Violation.Code.*;
import static org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.CodeMappingType.*;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.substractNullFriendly;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreString.normaliseString;

@Service("netsuite_adapter.TransactionLineConverter")
@RequiredArgsConstructor
@Slf4j
public class TransactionConverter {

    private final Validator validator;

    private final CodesMappingService codesMappingService;


    private final TransactionTypeMapper transactionTypeMapper;

    @Value("${lob.events.netsuite.to.core.netsuite.instance.id:fEU237r9rqAPEGEFY1yr}")
    private String netsuiteInstanceId;

    @Value("${lob.events.netsuite.financial.period.source:IMPLICIT}")
    private FinancialPeriodSource financialPeriodSource;

    public TransactionsWithViolations convert(String organisationId,
                                              String batchId,
                                              List<TxLine> txLines) {
        val searchResultsByOrganisation = new ArrayList<TxLine>();
        val transactions = new LinkedHashSet<Transaction>();
        val violations = new LinkedHashSet<Violation>();

        for (val txLine : txLines) {
            val organisationIdE = organisationId(txLine);

            if (organisationIdE.isEmpty()) {
                violations.add(organisationIdE.getLeft());
                continue;
            }

            val localOrgId = organisationIdE.get();

            if (localOrgId.equals(organisationId)) {
                searchResultsByOrganisation.add(txLine);
            }
        }

        val searchResultItemsPerTransactionNumber = searchResultsByOrganisation
                .stream()
                .collect(groupingBy(TxLine::transactionNumber));

        for (val entry : searchResultItemsPerTransactionNumber.entrySet()) {
            val txInternalNumber = entry.getKey();
            val transactionE = createTransactionFromSearchResultItems(organisationId, entry.getValue(), batchId);

            if (transactionE.isEmpty()) {
                violations.add(transactionE.getLeft());
                continue;
            }

            transactionE.get().ifPresent(transactions::add);
        }

        return new TransactionsWithViolations(organisationId, transactions, violations);
    }

    private Either<Violation, Optional<Transaction>> createTransactionFromSearchResultItems(String organisationId,
                                                                                            List<TxLine> txLines,
                                                                                            String batchId) {
        if (txLines.isEmpty()) {
            return Either.right(Optional.empty());
        }

        val firstTxLine = txLines.getFirst();
        val txId = Transaction.id(organisationId, firstTxLine.transactionNumber());

        val transTypeE = transactionType(organisationId, firstTxLine);
        if (transTypeE.isEmpty()) {
            return Either.left(transTypeE.getLeft());
        }
        val transactionType = transTypeE.get();

        val txDate = firstTxLine.date();
        val internalTransactionNumber = firstTxLine.transactionNumber();
        val fxRate = firstTxLine.exchangeRate();
        val accountingPeriod = financialPeriod(firstTxLine);

        val txItems = new LinkedHashSet<TransactionItem>();
        for (val txLine : txLines) {
            val validationIssues = validator.validate(txLine);
            val isValid = validationIssues.isEmpty();

            if (!isValid) {
                return Either.left(Violation.create(txLine, INVALID_TRANSACTION_LINE, Map.of(
                        "organisationId", organisationId,
                        "transactionNumber", txLine.transactionNumber()
                )));
            }

            val accountCreditCodeE = accountCreditCode(organisationId, txLine, txLine.accountMain());
            if (accountCreditCodeE.isLeft()) {
                return Either.left(accountCreditCodeE.getLeft());
            }

            val projectCodeM = projectCode(organisationId, txLine);
            if (projectCodeM.isEmpty()) {
                return Either.left(projectCodeM.getLeft());
            }

            val amountLcy = substractNullFriendly(txLine.amountDebit(), txLine.amountCredit());
            val amountFcy = substractNullFriendly(txLine.amountDebitForeignCurrency(), txLine.amountCreditForeignCurrency());

            val costCenterM = costCenterCode(organisationId, txLine);
            if (costCenterM.isLeft()) {
                return Either.left(costCenterM.getLeft());
            }

            val documentE = convertDocument(txLine);
            if (documentE.isLeft()) {
                return Either.left(documentE.getLeft());
            }

            val txItem = TransactionItem.builder()
                    .id(TransactionItem.id(txId, txLine.lineID().toString()))
                    .accountNameDebit(normaliseString(txLine.name()))
                    .accountCodeDebit(normaliseString(txLine.number()))
                    .accountCodeCredit(accountCreditCodeE.get())

                    .project(projectCodeM.get().map(pc -> Project.builder()
                            .customerCode(pc)
                            .build()
                    ))

                    .costCenter(costCenterM.get().map(cc -> CostCenter.builder()
                            .customerCode(cc)
                            .build()
                    ))
                    .document(documentE.get())

                    .amountLcy(amountLcy)
                    .amountFcy(amountFcy)

                    .build();

            txItems.add(txItem);
        }

        return Either.right(Optional.of(Transaction.builder()
                .id(txId)
                .internalTransactionNumber(internalTransactionNumber)
                .entryDate(txDate)
                .batchId(batchId)
                .transactionType(transactionType)
                .accountingPeriod(accountingPeriod)
                .organisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation.builder()
                        .id(organisationId)
                        .build()
                )
                .fxRate(fxRate)
                .items(txItems)
                .build())
        );
    }

    private YearMonth financialPeriod(TxLine txLine) {
        return switch (financialPeriodSource) {
            case IMPLICIT:
                yield YearMonth.from(txLine.date());
            case EXPLICIT: throw new UnsupportedOperationException("Explicit financial period source is not supported yet");
        };
    }

    private Either<Violation, TransactionType> transactionType(String organisationId,
                                                               TxLine txLine) {
        val transactionTypeM = transactionTypeMapper.apply(txLine.type());

        if (transactionTypeM.isEmpty()) {
            return Either.left(Violation.create(txLine, TRANSACTION_TYPE_MAPPING_NOT_FOUND, Map.of(
                    "organisationId", organisationId,
                    "type", txLine.type()
            )));
        }

        return Either.right(transactionTypeM.orElseThrow());
    }

    private Either<Violation, Optional<Document>> convertDocument(TxLine txLine) {
        val documentNumberM = normaliseString(txLine.documentNumber());

        if (documentNumberM.isPresent()) {
            val documentNumber = documentNumberM.orElseThrow();

            return Either.right(Optional.of(Document.builder()
                    .number(documentNumber)
                    .currency(Currency.builder()
                            .customerCode(txLine.currency())
                            .build())
                    .vat(vatCode(txLine).map(cc -> Vat.builder()
                            .customerCode(cc)
                            .build()))
                    .counterparty(convertCounterparty(txLine))
                    .build())
            );
        }

        return Either.right(Optional.empty());
    }

    private static Optional<Counterparty> convertCounterparty(TxLine txLine) {
        return normaliseString(txLine.id()).map(customerCode -> Counterparty.builder()
                .customerCode(customerCode)
                .type(VENDOR) // TODO CF hardcoded for now
                .name(normaliseString(txLine.companyName()))
                .build());
    }

    private Either<Violation, Optional<String>> accountCreditCode(String organisationId,
                                                                  TxLine txLine,
                                                                  String accountMain) {
        val accountCodeCreditM = normaliseString(accountMain)
                .map(Long::parseLong);

        if (accountCodeCreditM.isPresent()) {
            val internalId = accountCodeCreditM.orElseThrow();

            val accountCreditCodeM = codesMappingService.getCodeMapping(organisationId, internalId, CHART_OF_ACCOUNT);

            if (accountCreditCodeM.isPresent()) {
                return Either.right(accountCreditCodeM);
            }

            return Either.left(Violation.create(txLine, CHART_OF_ACCOUNT_NOT_FOUND, Map.of(
                    "organisationId", organisationId,
                    "internalId", internalId
            )));
        }

        return Either.right(Optional.empty());
    }

    private Either<Violation, Optional<String>> costCenterCode(String organisationId,
                                                               TxLine txLine) {
        val costCenterM = normaliseString(txLine.costCenter())
                .map(Long::parseLong);

        if (costCenterM.isPresent()) {
            val internalId = costCenterM.orElseThrow();
            val costCenterE = codesMappingService.getCodeMapping(organisationId, internalId, COST_CENTER);

            if (costCenterE.isPresent()) {
                return Either.right(costCenterE);
            }

            return Either.left(Violation.create(txLine, COST_CENTER_NOT_FOUND, Map.of(
                    "organisationId", organisationId,
                    "internalId", internalId
            )));
        }

        return Either.right(Optional.empty());
    }

    private Either<Violation, String> organisationId(TxLine txLine) {
        val organisationIdM = codesMappingService.getCodeMapping(netsuiteInstanceId, txLine.subsidiary(), ORGANISATION);

        if (organisationIdM.isEmpty()) {
            return Either.left(Violation.create(txLine, ORGANISATION_MAPPING_NOT_FOUND, Map.of(
                    "netsuiteInstanceId", netsuiteInstanceId,
                    "internalId", txLine.subsidiary()
            )));
        }

        return Either.right(organisationIdM.orElseThrow());
    }

    private Optional<String> vatCode(TxLine txLine) {
        return normaliseString(txLine.taxItem())
                .map(String::trim);
    }

    private Either<Violation, Optional<String>> projectCode(String organisationId,
                                                            TxLine txLine) {
        val projectInternalIdM = normaliseString(txLine.project()).map(Long::parseLong);

        if (projectInternalIdM.isPresent()) {
            val internalId = projectInternalIdM.orElseThrow();

            val projectCodeM = codesMappingService.getCodeMapping(organisationId, internalId, PROJECT);

            if (projectCodeM.isEmpty()) {
                return Either.left(Violation.create(txLine, PROJECT_MAPPING_NOT_FOUND, Map.of(
                        "organisationId", organisationId,
                        "internalId", internalId)));
            }

            return Either.right(projectCodeM);
        }

        return Either.right(Optional.empty());
    }

}

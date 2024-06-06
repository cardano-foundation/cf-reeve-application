package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service;

import io.vavr.control.Either;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FinancialPeriodSource;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.TransactionsWithViolations;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.TxLine;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.Violation;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.CodeMappingType;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util.MoreBigDecimal;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util.MoreString;
import org.cardanofoundation.lob.app.support.collections.Optionals;

import java.time.YearMonth;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type.VENDOR;
import static org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FieldType.*;

@RequiredArgsConstructor
@Slf4j
public class TransactionConverter {

    private final Validator validator;

    private final CodesMappingService codesMappingService;

    private final PreprocessorService preprocessorService;

    private final TransactionTypeMapper transactionTypeMapper;

    private final String netsuiteInstanceId;

    private final FinancialPeriodSource financialPeriodSource;

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
            val transactionLevelTxLines = entry.getValue();
            val transactionE = createTransactionFromSearchResultItems(organisationId, batchId, transactionLevelTxLines);

            if (transactionE.isEmpty()) {
                violations.add(transactionE.getLeft());
                continue;
            }

            transactionE.get().ifPresent(transactions::add);
        }

        return new TransactionsWithViolations(organisationId, transactions, violations);
    }

    private Either<Violation, Optional<Transaction>> createTransactionFromSearchResultItems(String organisationId,
                                                                                            String batchId,
                                                                                            List<TxLine> txLines
    ) {
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
                return Either.left(Violation.create(txLine, Violation.Code.INVALID_TRANSACTION_LINE, Map.of(
                        "organisationId", organisationId,
                        "transactionNumber", txLine.transactionNumber()
                )));
            }

            val accountCreditCodeE = accountCreditCode(organisationId, txLine, txLine.accountMain());
            if (accountCreditCodeE.isLeft()) {
                return Either.left(accountCreditCodeE.getLeft());
            }
            val accountCreditCodeM = accountCreditCodeE.get();

            val amountLcy = MoreBigDecimal.substractNullFriendly(txLine.amountDebit(), txLine.amountCredit());
            val amountFcy = MoreBigDecimal.substractNullFriendly(txLine.amountDebitForeignCurrency(), txLine.amountCreditForeignCurrency());

            val costCenterM = costCenterCode(organisationId, txLine);
            if (costCenterM.isLeft()) {
                return Either.left(costCenterM.getLeft());
            }

            val projectCodeM = projectCode(organisationId, txLine);
            if (projectCodeM.isEmpty()) {
                return Either.left(projectCodeM.getLeft());
            }

            val documentE = convertDocument(organisationId, txLine);
            if (documentE.isLeft()) {
                return Either.left(documentE.getLeft());
            }

            val txItem = TransactionItem.builder()
                    .id(TransactionItem.id(txId, txLine.lineID().toString()))

                    .accountDebit(Optionals.zip(MoreString.normaliseString(txLine.name()), MoreString.normaliseString(txLine.number()), (name, number) -> {
                        return Account.builder()
                                .name(Optional.of(name))
                                .code(number)
                                .build();
                    }))

                    .accountCredit(accountCreditCodeM.map(accountCreditCode -> {
                                return Account.builder()
                                        .code(accountCreditCode)
                                        .build();
                            })
                    )

                    .project(projectCodeM.get().map(pc -> Project.builder()
                            .customerCode(pc)
                            .build()
                    ))

                    .costCenter(costCenterM.get().map(cc -> CostCenter.builder()
                            .customerCode(cc)
                            .build()
                    ))
                    .document(documentE.get())

                    .fxRate(fxRate)

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
                .items(txItems)
                .build())
        );
    }

    private YearMonth financialPeriod(TxLine txLine) {
        return switch (financialPeriodSource) {
            case IMPLICIT -> YearMonth.from(txLine.date());
            case EXPLICIT -> {
                val endDate = txLine.endDate();

                yield YearMonth.from(endDate);
            }
        };
    }

    private Either<Violation, TransactionType> transactionType(String organisationId,
                                                               TxLine txLine) {
        val transactionTypeM = transactionTypeMapper.apply(txLine.type());

        if (transactionTypeM.isEmpty()) {
            return Either.left(Violation.create(txLine, Violation.Code.TRANSACTION_TYPE_MAPPING_NOT_FOUND, Map.of(
                    "organisationId", organisationId,
                    "type", txLine.type()
            )));
        }

        return Either.right(transactionTypeM.orElseThrow());
    }

    private Either<Violation, Optional<Document>> convertDocument(String organisationId, TxLine txLine) {
        val documentNumberM = MoreString.normaliseString(txLine.documentNumber());

        if (documentNumberM.isPresent()) {
            val documentNumber = documentNumberM.orElseThrow();

            val taxItemM = MoreString.normaliseString(txLine.taxItem())
                    .map(String::trim);

            var vatCodeM = Optional.<String>empty();
            if (taxItemM.isPresent()) {
                val vatCodeE = preprocessorService.preProcess(taxItemM.orElseThrow(), VAT);

                if (vatCodeE.isEmpty()) {
                    return Either.left(Violation.create(txLine, Violation.Code.VAT_MAPPING_NOT_FOUND, Map.of(
                            "organisationId", organisationId,
                            "taxItem", taxItemM.orElseThrow()
                    )));
                }

                vatCodeM = Optional.of(vatCodeE.get());
            }

            return Either.right(Optional.of(Document.builder()
                    .number(documentNumber)
                    .currency(Currency.builder()
                            .customerCode(txLine.currencySymbol())
                            .build())
                    .vat(vatCodeM.map(cc -> Vat.builder()
                            .customerCode(cc)
                            .build()))
                    .counterparty(convertCounterparty(txLine))
                    .build())
            );
        }

        return Either.right(Optional.empty());
    }

    private static Optional<Counterparty> convertCounterparty(TxLine txLine) {
        return MoreString.normaliseString(txLine.id()).map(customerCode -> Counterparty.builder()
                .customerCode(customerCode)
                .type(VENDOR) // TODO CF hardcoded for now
                .name(MoreString.normaliseString(txLine.companyName()))
                .build());
    }

    private Either<Violation, Optional<String>> accountCreditCode(String organisationId,
                                                                  TxLine txLine,
                                                                  String accountMain) {
        val accountCodeCreditM = MoreString.normaliseString(accountMain);

        if (accountCodeCreditM.isPresent()) {
            val accountCodeCredit = accountCodeCreditM.orElseThrow();

            val accountCreditCodeE = preprocessorService.preProcess(accountCodeCreditM.orElseThrow(), CHART_OF_ACCOUNT);

            if (accountCreditCodeE.isEmpty()) {
                return Either.left(Violation.create(txLine, Violation.Code.CHART_OF_ACCOUNT_NOT_FOUND, Map.of(
                        "organisationId", organisationId,
                        "accountCodeCredit", accountCodeCredit
                )));
            }

            val accountCreditCode = accountCreditCodeE.get();

            return Either.right(Optional.of(accountCreditCode));
        }

        return Either.right(Optional.empty());
    }

    private Either<Violation, Optional<String>> costCenterCode(String organisationId,
                                                               TxLine txLine) {
        val costCenterM = MoreString.normaliseString(txLine.costCenter());

        if (costCenterM.isPresent()) {
            val internalId = costCenterM.orElseThrow();
            val costCenterE = preprocessorService.preProcess(txLine.costCenter(), COST_CENTER);

            if (costCenterE.isEmpty()) {
                return Either.left(Violation.create(txLine, Violation.Code.COST_CENTER_NOT_FOUND, Map.of(
                        "organisationId", organisationId,
                        "internalId", internalId
                )));
            }
            val costCenter = costCenterE.get();

            return Either.right(Optional.of(costCenter));
        }

        return Either.right(Optional.empty());
    }

    private Either<Violation, String> organisationId(TxLine txLine) {
        val organisationIdM = codesMappingService.getCodeMapping(netsuiteInstanceId, txLine.subsidiary(), CodeMappingType.ORGANISATION);

        if (organisationIdM.isEmpty()) {
            return Either.left(Violation.create(txLine, Violation.Code.ORGANISATION_MAPPING_NOT_FOUND, Map.of(
                    "netsuiteInstanceId", netsuiteInstanceId,
                    "subsidiaryId", txLine.subsidiary().toString()
            )));
        }

        return Either.right(organisationIdM.orElseThrow());
    }

    private Either<Violation, Optional<String>> projectCode(String organisationId,
                                                            TxLine txLine) {
        val projectM = MoreString.normaliseString(txLine.project());

        if (projectM.isPresent()) {
            val projectText = projectM.orElseThrow();

            val projectCodeE = preprocessorService.preProcess(projectM.orElseThrow(), PROJECT);

            if (projectCodeE.isEmpty()) {
                return Either.left(Violation.create(txLine, Violation.Code.PROJECT_MAPPING_NOT_FOUND, Map.of(
                        "organisationId", organisationId,
                        "project", projectText)));
            }

            val projectCode = projectCodeE.get();

            return Either.right(Optional.of(projectCode));
        }

        return Either.right(Optional.empty());
    }

}

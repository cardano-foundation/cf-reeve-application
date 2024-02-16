package org.cardanofoundation.lob.app.netsuite_adapter.service;

import io.vavr.control.Either;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.SearchResultTransactionItem;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.NOT_VALIDATED;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.substractNullFriendly;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreString.normaliseString;
import static org.cardanofoundation.lob.app.organisation.domain.core.AccountSystemProvider.NETSUITE;

@Service("netsuite_adapter.TransactionLineConverter")
@RequiredArgsConstructor
@Slf4j
public class TransactionConverter {

    private final OrganisationPublicApi organisationPublicApi;

    private final Validator validator;

    private final TransactionTypeMapper transactionTypeMapper;

    // TODO this this over properly
    @Value("${lob.connector.id:jhu765}")
    private String connectorId;

    // split results across multiple organisations
    public Either<Problem, Map<String, Set<Transaction>>> convert(List<SearchResultTransactionItem> searchResultTransactionItems) {
        log.info("transactionDataSearchResult count:{}", searchResultTransactionItems.size());

        val searchResultsByOrganisation = new ArrayList<OrganisationSearchResults>();
        for (val searchResultItem : searchResultTransactionItems) {
            val validationIssues = validator.validate(searchResultItem);
            val isValid = validationIssues.isEmpty();

            if (!isValid) {
                log.error("Invalid netsuite transaction line item: {}", searchResultItem);

                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::INVALID_TRANSACTION_LINE")
                        .withDetail(STR."Invalid netsuite transaction line item, line: \{searchResultItem}")
                        .build();

                return Either.left(issue);
            }

            val organisationId = Organisation.id(NETSUITE, connectorId, String.valueOf(searchResultItem.subsidiary()));
            val organisationM = organisationPublicApi.findByOrganisationId(organisationId);

            if (organisationM.isEmpty()) {
                log.error("Organisation mapping not found for organisationId: {}", organisationId);

                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::ORGANISATION_MAPPING_NOT_FOUND")
                        .withDetail(STR."Organisation mapping not found for organisationId: \{organisationId}, subsidary: \{searchResultItem.subsidiary()}")
                        .build();

                return Either.left(issue);
            }

            val organisation = organisationM.orElseThrow();

            searchResultsByOrganisation.add(new OrganisationSearchResults(organisation, searchResultItem));
        }

        val searchResultsByOrganisationMap = searchResultsByOrganisation.stream()
                .collect(groupingBy(OrganisationSearchResults::organisation));

        val transactionsByOrganisations = new HashMap<String, Set<Transaction>>();
        for (val entry : searchResultsByOrganisationMap.entrySet()) {
            val organisation = entry.getKey();
            val organisationSearchResults = entry.getValue()
                    .stream()
                    .map(OrganisationSearchResults::searchResultTransactionItem)
                    .toList();

            val searchResultItemsPerTransactionNumber = organisationSearchResults
                    .stream()
                    .collect(groupingBy(SearchResultTransactionItem::transactionNumber));

            for (val transactionNumberEntry : searchResultItemsPerTransactionNumber.entrySet()) {
                val searchResultItems = transactionNumberEntry.getValue();

                val transactionE = createTransactionFromSearchResultItems(organisation, searchResultItems);

                if (transactionE.isLeft()) {
                    return Either.left(transactionE.getLeft());
                }

                val transactionM = transactionE.get();

                transactionM.ifPresent(transaction -> {
                    transactionsByOrganisations
                            .computeIfAbsent(organisation.id(), id -> new HashSet<>())
                            .add(transaction);
                });
            }

            log.info("transactionsByOrganisations count: {}", transactionsByOrganisations.size());
        }

        return Either.right(transactionsByOrganisations);
    }

    private Either<Problem, Optional<Transaction>> createTransactionFromSearchResultItems(Organisation organisation,
                                                                                          List<SearchResultTransactionItem> results) {
        if (results.isEmpty()) {
            return Either.right(Optional.empty());
        }
//
//        for (val result : results) {
//            log.info("tx_number: {}, tx_line_id:{}, project:{}", result.transactionNumber(), result.lineID(), result.project());
//        }

        val first = results.getFirst();

        val txItems = results.stream().map(result -> {
                    val txLineId = TransactionItem.id(organisation.id(), result.transactionNumber(), result.lineID().toString());

                    return TransactionItem.builder()
                            .id(txLineId)
                            .accountCodeCredit(normaliseString(result.accountMain()))
                            .accountNameDebit(normaliseString(result.name()))
                            .accountCodeDebit(normaliseString(result.number()))
                            .amountLcy(substractNullFriendly(result.amountDebit(), result.amountCredit()))
                            .amountFcy(substractNullFriendly(result.amountDebitForeignCurrency(), result.amountCreditForeignCurrency()))
                            .build();
                })
                .collect(Collectors.toSet());

        val baseCurrency = organisation.baseCurrency();
        val organisationCurrency = new Currency(Optional.of(baseCurrency.currencyId()), baseCurrency.internalNumber());

        var documentInternalNumberM = normaliseString(first.documentNumber());

        if (documentInternalNumberM.isEmpty()) {
            val issue = Problem.builder()
                    .withTitle("NETSUITE_ADAPTER::MISSING_DOCUMENT_NUMBER")
                    .withDetail(STR."Missing document number for transaction: \{first.transactionNumber()}")
                    .build();
            log.warn("tx line issue: {}", issue);

            return Either.right(Optional.empty());

//            return Either.left(issue);
        }

        return Either.right(Optional.of(Transaction.builder()
                .id(Transaction.id(organisation.id(), first.transactionNumber()))
                .internalTransactionNumber(first.transactionNumber())
                .entryDate(first.date())
                .transactionType(transactionTypeMapper.apply(first.type()))
                .organisation(new org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation(organisation.id(), organisationCurrency))
                .costCenterInternalNumber(normaliseString(first.costCenter()))
                .projectInternalNumber(normaliseString(first.project()))
                .fxRate(first.exchangeRate())
                .ledgerDispatchApproved(true) // TODO remove this, for now only for testing
                .validationStatus(NOT_VALIDATED)
                .document(Document.builder()
                        .internalNumber(first.documentNumber())
                        .currency(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.from(String.valueOf(first.currency())))
                        .vat(results.stream().filter(r -> normaliseString(r.taxItem()).isPresent()).findFirst().map(SearchResultTransactionItem::taxItem).map(vatInternalNumber -> Vat.builder()
                                .internalNumber(vatInternalNumber)
                                .build()))
                        .counterparty(convertCounterparty(first))
                        .build())
                .transactionItems(txItems)
                .build()));
    }

    private static Optional<Counterparty> convertCounterparty(SearchResultTransactionItem first) {
        return normaliseString(first.id()).map(internalNumber -> new Counterparty(internalNumber, Optional.of(first.companyName())));
    }

    private record OrganisationSearchResults(Organisation organisation,
                                             SearchResultTransactionItem searchResultTransactionItem) {
    }

}

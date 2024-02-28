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
import static org.cardanofoundation.lob.app.organisation.domain.core.ERPDataSource.NETSUITE;

@Service("netsuite_adapter.TransactionLineConverter")
@RequiredArgsConstructor
@Slf4j
public class TransactionConverter {

    private final OrganisationPublicApi organisationPublicApi;

    private final Validator validator;

    private final TransactionTypeMapper transactionTypeMapper;

    @Value("${lob.netsuite.connector.id:jhu765}")
    private String netsuiteConnectorId;

    // split results across multiple organisations
    public Either<Problem, Map<String, Set<Transaction>>> convert(List<SearchResultTransactionItem> searchResultTransactionItems) {
        log.info("transactionDataSearchResult count:{}", searchResultTransactionItems.size());

        val searchResultsByOrganisation = new ArrayList<OrganisationSearchResults>();
        for (val searchResultItem : searchResultTransactionItems) {
            val organisationId = Organisation.id(NETSUITE, netsuiteConnectorId, String.valueOf(searchResultItem.subsidiary()));
            val organisationM = organisationPublicApi.findByOrganisationId(organisationId);

            if (organisationM.isEmpty()) {
                log.error("Organisation mapping not found for organisationId: {}", organisationId);

                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::ORGANISATION_MAPPING_NOT_FOUND")
                        .withDetail(STR."Organisation mapping not found for organisationId: \{organisationId}, subsidary: \{searchResultItem.subsidiary()}")
                        .build();

                return Either.left(issue);
            }

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

                val transactionM = createTransactionFromSearchResultItems(organisation, searchResultItems);

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

    private Optional<Transaction> createTransactionFromSearchResultItems(Organisation organisation,
                                                                         List<SearchResultTransactionItem> results) {
        if (results.isEmpty()) {
            return Optional.empty();
        }

        val first = results.getFirst();

        val txId = Transaction.id(organisation.id(), first.transactionNumber());

        val txItems = results.stream().map(result -> {
                    val txLineId = TransactionItem.id(txId, result.lineID().toString());

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

        val baseCurrency = organisation.currency();
        val organisationCurrency = new Currency(Optional.of(baseCurrency.currencyId()),
                baseCurrency.internalNumber());

        return Optional.of(Transaction.builder()
                .id(Transaction.id(organisation.id(), first.transactionNumber()))
                .internalTransactionNumber(first.transactionNumber())
                .entryDate(first.date())
                .transactionType(transactionTypeMapper.apply(first.type()))
                .organisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation.builder()
                        .id(organisation.id())
                        .shortName(organisation.shortName())
                        .currency(organisationCurrency).build()
                )
                .costCenter(normaliseString(first.costCenter()).map(internalNumber -> {
                    return CostCenter.builder()
                            .internalNumber(internalNumber)
                            .build();
                }))
                .project(normaliseString(first.project()).map(internalNumber -> {
                    return Project.builder()
                            .internalNumber(internalNumber)
                            .build();
                }))
                .fxRate(first.exchangeRate())
                .validationStatus(NOT_VALIDATED)
                .ledgerDispatchApproved(false)
                .document(convertDocument(results, first))
                .transactionItems(txItems)
                .build());
    }

    private static Optional<Document> convertDocument(List<SearchResultTransactionItem> results,
                                                      SearchResultTransactionItem first) {
        return normaliseString(first.documentNumber()).map(internalDocumentNumber -> {
            val vatM = results.stream()
                    .filter(r -> normaliseString(r.taxItem()).isPresent()).findFirst()
                    .map(SearchResultTransactionItem::taxItem)
                    .map(vatInternalNumber -> Vat.builder()
                            .internalNumber(vatInternalNumber)
                            .build());

            return Document.builder()
                    .internalNumber(internalDocumentNumber)
                    .currency(Currency.from(String.valueOf(first.currency())))
                    .vat(vatM)
                    .counterparty(convertCounterparty(first))
                    .build();
        });
    }

    private static Optional<Counterparty> convertCounterparty(SearchResultTransactionItem first) {
        return normaliseString(first.id()).map(internalNumber -> new Counterparty(internalNumber, Optional.of(first.companyName())));
    }

    private record OrganisationSearchResults(Organisation organisation,
                                             SearchResultTransactionItem searchResultTransactionItem) {
    }

}

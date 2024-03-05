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
import org.cardanofoundation.lob.app.organisation.domain.core.CharterOfAccounts;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.NOT_VALIDATED;
import static org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.CodeMappingType.CURRENCY;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.substractNullFriendly;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreString.normaliseString;
import static org.cardanofoundation.lob.app.organisation.domain.core.ERPDataSource.NETSUITE;

@Service("netsuite_adapter.TransactionLineConverter")
@RequiredArgsConstructor
@Slf4j
public class TransactionConverter {

    private final OrganisationPublicApi organisationPublicApi;

    private final CodesMappingService codesMappingService;

    private final Validator validator;

    private final TransactionTypeMapper transactionTypeMapper;

    // split results across multiple organisations
    public Either<List<Problem>, Map<String, Set<Transaction>>> convert(List<SearchResultTransactionItem> searchResultTransactionItems) {
        log.info("transactionDataSearchResult count:{}", searchResultTransactionItems.size());

        val searchResultsByOrganisation = new ArrayList<OrganisationSearchResults>();
        for (val searchResultItem : searchResultTransactionItems) {
            val organisationM = organisationPublicApi.findByErpInternalNumber(NETSUITE, String.valueOf(searchResultItem.subsidiary()));
            if (organisationM.isEmpty()) {
                log.error("Organisation mapping not found for netsuite internal number: {}", searchResultItem.subsidiary());

                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::ORGANISATION_MAPPING_NOT_FOUND")
                        .withDetail(STR."Organisation mapping not found for netsuite id: \{searchResultItem.subsidiary()}")
                        .build();

                return Either.left(List.of(issue));
            }

            val validationIssues = validator.validate(searchResultItem);
            val isValid = validationIssues.isEmpty();

            if (!isValid) {
                log.error("Invalid netsuite transaction line item: {}", searchResultItem);

                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::INVALID_TRANSACTION_LINE")
                        .withDetail(STR."Invalid netsuite transaction line item, line: \{searchResultItem}")
                        .build();

                return Either.left(List.of(issue));
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

                if (transactionE.isEmpty()) {
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

    private Either<List<Problem>, Optional<Transaction>> createTransactionFromSearchResultItems(Organisation organisation,
                                                                                                List<SearchResultTransactionItem> results) {
        if (results.isEmpty()) {
            return Either.right(Optional.empty());
        }

        val problems = new ArrayList<Problem>();

        val first = results.getFirst();

        val txId = Transaction.id(organisation.id(), first.transactionNumber());

        val txItems = new LinkedHashSet<TransactionItem>();
        for (val result : results) {
            val validationIssues = validator.validate(result);
            val isValid = validationIssues.isEmpty();

            if (!isValid) {
                log.error("Invalid netsuite transaction line item: {}", result);

                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::INVALID_TRANSACTION_LINE")
                        .withDetail(STR."Invalid netsuite transaction line item, line: \{result}")
                        .build();

                problems.add(issue);

                continue;
            }

            val txLineId = TransactionItem.id(txId, result.lineID().toString());

            val accountCharterE = accountCredit(result.accountMain());

            if (accountCharterE.isLeft()) {
                problems.add(accountCharterE.getLeft());
                continue;
            }

            val amountLcy = substractNullFriendly(result.amountDebit(), result.amountCredit());
            val amountFcy = substractNullFriendly(result.amountDebitForeignCurrency(), result.amountCreditForeignCurrency());

            val txLine = TransactionItem.builder()
                    .id(txLineId)
                    .accountNameDebit(normaliseString(result.name()))
                    .accountCodeDebit(normaliseString(result.number()))
                    .accountCodeCredit(accountCharterE.get().map(CharterOfAccounts::code))

                    .amountLcy(amountLcy)
                    .amountFcy(amountFcy)

                    .build();

            txItems.add(txLine);
        }

        val documentE = convertDocument(organisation, results, first);
        if (documentE.isLeft()) {
            problems.addAll(documentE.getLeft());
        }

        if (!problems.isEmpty()) {
            return Either.left(problems);
        }

        return Either.right(Optional.of(Transaction.builder()
                .id(Transaction.id(organisation.id(), first.transactionNumber()))
                .internalTransactionNumber(first.transactionNumber())
                .entryDate(first.date())
                .transactionType(transactionTypeMapper.apply(first.type()))
                .organisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation.builder()
                        .id(organisation.id())
                        .shortName(organisation.shortName())
                        .currency(Currency.fromId(organisation.currency().toId())).build()
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
                .transactionApproved(false)
                .document(documentE.get())
                .transactionItems(txItems)
                .build())
        );
    }

    private Either<List<Problem>, Optional<Document>> convertDocument(Organisation organisation,
                                                                      List<SearchResultTransactionItem> results,
                                                                      SearchResultTransactionItem first) {
        val documentNumberM = normaliseString(first.documentNumber());

        if (documentNumberM.isPresent()) {
            val problems = new ArrayList<Problem>();

            val internalDocumentNumber = documentNumberM.orElseThrow();

            val vatM = results.stream()
                    .filter(r -> normaliseString(r.taxItem()).isPresent()).findFirst()
                    .map(SearchResultTransactionItem::taxItem)
                    .map(vatInternalNumber -> Vat.builder()
                            .internalNumber(vatInternalNumber)
                            .build());

            val currencyIdM = codesMappingService.getCodeMapping(organisation.id(), first.currency(), CURRENCY);

            if (currencyIdM.isEmpty()) {
                log.info("Currency mapping not found for netsuite internal number: {}", first.currency());

                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::CURRENCY_MAPPING_NOT_FOUND")
                        .withDetail(STR."Currency mapping not found for netsuite id: \{first.currency()}")
                        .with("organisationId", organisation.id())
                        .with("currencyId", first.currency())
                        .build();

                problems.add(issue);

                return Either.left(problems);
            }

            val currencyId = currencyIdM.orElseThrow();

            return Either.right(Optional.of(Document.builder()
                    .internalNumber(internalDocumentNumber)
                    .currency(Currency.fromId(currencyId))
                    .vat(vatM)
                    .counterparty(convertCounterparty(first))
                    .build()));
        }

        return Either.right(Optional.empty());
    }

    private static Optional<Counterparty> convertCounterparty(SearchResultTransactionItem first) {
        return normaliseString(first.id()).map(internalNumber -> new Counterparty(internalNumber, Optional.of(first.companyName())));
    }

    private record OrganisationSearchResults(Organisation organisation,
                                             SearchResultTransactionItem searchResultTransactionItem) {
    }

    private Either<Problem, Optional<CharterOfAccounts>> accountCredit(String accountMain) {
        val accountCodeCreditM = normaliseString(accountMain);

        if (accountCodeCreditM.isPresent()) {
            val charterAccountM = organisationPublicApi.getChartOfAccounts(NETSUITE, accountCodeCreditM.orElseThrow());

            if (charterAccountM.isPresent()) {
                return Either.right(charterAccountM);
            }

            return Either.left(Problem.builder()
                    .withTitle("NETSUITE_ADAPTER::CHARTER_ACCOUNT_MAPPING_NOT_FOUND")
                    .withDetail(STR."Account mapping not found for netsuite id: \{accountMain}")
                    .build());

        }

        return Either.right(Optional.empty());
    }

}

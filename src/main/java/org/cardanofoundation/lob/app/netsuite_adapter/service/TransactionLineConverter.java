package org.cardanofoundation.lob.app.netsuite_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ACLMappingException;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.SearchResultTransactionItem;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.Type;
import org.cardanofoundation.lob.app.netsuite_adapter.util.SHA3;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.LedgerDispatchStatus.NOT_DISPATCHED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.NOT_VALIDATED;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.substractOpt;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.zeroForNull;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreString.normaliseString;
import static org.cardanofoundation.lob.app.organisation.domain.core.AccountSystemProvider.NETSUITE;

@Service("netsuite_adapter.TransactionLineConverter")
@RequiredArgsConstructor
@Slf4j
public class TransactionLineConverter {

    private final OrganisationPublicApi organisationPublicApi;

    public TransactionLine convert(UUID ingestionId,
                                   SearchResultTransactionItem searchResultTransactionItem) {
        // TODO what if this id is 1 for multiple organisations?
        // we need some concept of a realm or something

        val organisationM = organisationPublicApi.findByForeignProvider(String.valueOf(searchResultTransactionItem.subsidiary()), NETSUITE);

        if (organisationM.isEmpty()) {
            throw new ACLMappingException(STR."Organisation mapping not found for subsidiaryId: \{searchResultTransactionItem.subsidiary()}");
        }

        val organisation = organisationM.orElseThrow();
        val organisationId = organisationM.orElseThrow().id();

        return new TransactionLine(
                createTxLineId(organisation, searchResultTransactionItem),
                organisationId,
                transactionType(searchResultTransactionItem.type()),
                searchResultTransactionItem.date(),
                searchResultTransactionItem.transactionNumber(),
                ingestionId,
                searchResultTransactionItem.number(),
                organisation.baseCurrency().internalId(),
                organisation.baseCurrency().currencyId(),
                String.valueOf(searchResultTransactionItem.currency()),
                Optional.empty(),
                searchResultTransactionItem.exchangeRate(),
                NOT_DISPATCHED,
                normaliseString(searchResultTransactionItem.documentNumber()),
                normaliseString(searchResultTransactionItem.id()),
                normaliseString(searchResultTransactionItem.companyName()),
                normaliseString(searchResultTransactionItem.costCenter()),
                normaliseString(searchResultTransactionItem.project()),
                normaliseString(searchResultTransactionItem.taxItem()),
                Optional.empty(),
                normaliseString(searchResultTransactionItem.name()),
                normaliseString(searchResultTransactionItem.accountMain()),
                NOT_VALIDATED,
                substractOpt(zeroForNull(searchResultTransactionItem.amountDebitForeignCurrency()), zeroForNull(searchResultTransactionItem.amountCreditForeignCurrency())),
                substractOpt(zeroForNull(searchResultTransactionItem.amountDebit()), zeroForNull(searchResultTransactionItem.amountCredit()))
        );
    }

//    private TransactionLine.CurrencyPair covertOrganisationCurrency(Organisation organisation) {
//        val organisationBaseCurrency = organisation.baseCurrency();
//        val organisationBaseCurrencyId = organisationBaseCurrency.currencyId();
//
//        return TransactionLine.CurrencyPair(organisationBaseCurrency, Optional.empty());
//    }
//
//    private TransactionLine.CurrencyPair convertCurrency(SearchResultTransactionItem item) {
//        val currencyInternalId = item.currency();
//
//        val currencyM = organisationPublicApi.findOrganisationCurrencyByInternalId(currencyInternalId.toString()).flatMap(organisationCurrency -> {
//            val currencyId = organisationCurrency.currencyId();
//
//            return organisationPublicApi.findByCurrencyId(currencyId)
//                    .map(currency -> new TransactionLine.CurrencyPair(organisationCurrency, currency));
//        });
//
//        if (currencyM.isEmpty()) {
//            throw new ACLMappingException(STR."Currency mapping not found for internalCurrencyId: \{currencyInternalId}");
//        }
//
//        return currencyM.orElseThrow();
//    }
//
//    private Optional<TransactionLine.VatPair> convertVat(SearchResultTransactionItem searchResultTransactionItem) {
//        val vatRateCodeM = normaliseString(searchResultTransactionItem.taxItem());
//
//        if (vatRateCodeM.isEmpty()) {
//            return Optional.empty();
//        }
//
//        val internalVatId = vatRateCodeM.orElseThrow();
//
//        val organisationVatM = organisationPublicApi.findOrganisationVatByInternalId(internalVatId);
//
//        if (organisationVatM.isEmpty()) {
//            log.error("Vat organisationVat not found for internalVatId: {}", internalVatId);
//
//            applicationEventPublisher.publishEvent(NotificationEvent.create(
//                    ERROR,
//                    "VAT_RATE_NOT_FOUND_ERROR",
//                    STR."Vat Rate not found for internalVatId: \{internalVatId}",
//                    STR."Vat Rate not found for internalVatId: \{internalVatId}"));
//
//            throw new ACLMappingException(STR."Vat Rate mapping not found for internalVatId: \{internalVatId}");
//        }
//
//        val organisationVat = organisationVatM.orElseThrow();
//
//        return Optional.of(new TransactionLine.VatPair(internalVatId, organisationVat.rate()));
//    }

    public String createTxLineId(Organisation organisation, SearchResultTransactionItem searchResultTransactionItem) {
        val transactionNumber  = searchResultTransactionItem.transactionNumber();

        return SHA3.digest(String.format("%s::%s::%s::%s", NETSUITE, organisation.id(), transactionNumber, searchResultTransactionItem.lineID()));
    }

    private TransactionType transactionType(Type transType) {
        return switch(transType) {
            case CardChrg -> TransactionType.CardCharge;
            case VendBill -> TransactionType.VendorBill;
            case CardRfnd -> TransactionType.CardRefund;
            case Journal -> TransactionType.Journal;
            case FxReval -> TransactionType.FxRevaluation;
            case Transfer -> TransactionType.Transfer;
            case CustPymt -> TransactionType.CustomerPayment;
        };
    }

}

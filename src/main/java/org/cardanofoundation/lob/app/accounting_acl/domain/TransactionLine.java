package org.cardanofoundation.lob.app.accounting_acl.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.cardanofoundation.lob.app.netsuite.util.NetSuiteDateDeserialiser;

import java.time.LocalDateTime;

public record TransactionLine(

        @JsonProperty("ID") String id,

        @JsonProperty("Subsidiary (no hierarchy)") String subsidiary,

        @JsonProperty("Type") String type,

        @JsonProperty("Date Created")
        LocalDateTime dateCreated,

        @JsonProperty("Company Name") String companyName,

        @JsonProperty("Tax Item") String taxItem,

        @JsonProperty("Cost Center (no hierarchy)") String costCenter,

        @JsonProperty("Transaction Number") String transactionNumber,

        @JsonProperty("Document Number") String documentNumber,

        @JsonProperty("Number") String number,

        @JsonProperty("Name") String name,

        @JsonProperty("Project (no hierarchy)") String project,

        @JsonProperty("Account (Main)") String accountMain,

        @JsonProperty("Memo (Main)") String memoMain,

        @JsonProperty("Currency") String currency,

        @JsonProperty("Exchange Rate") String exchangeRate,

        @JsonProperty("Amount (Debit) (Foreign Currency)") String amountDebitForeignCurrency,

        @JsonProperty("Amount (Credit) (Foreign Currency)") String amountCreditForeignCurrency,

        @JsonProperty("Amount (Debit)") String amountDebit,

        @JsonProperty("Amount (Credit)") String amountCredit) {

}

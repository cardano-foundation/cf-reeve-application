package org.cardanofoundation.lob.app.netsuite.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.cardanofoundation.lob.app.netsuite.util.NetSuiteDateDeserialiser;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionLine(

        @JsonProperty("Subsidiary (no hierarchy)") String subsidiary,
        @JsonProperty("Type") String type,

        @JsonProperty("Date Created")
        @JsonDeserialize(using = NetSuiteDateDeserialiser.class)
        LocalDateTime dateCreated,

        @JsonProperty("End Date")
        @JsonDeserialize(using = NetSuiteDateDeserialiser.class)
        LocalDateTime endDate,

        @JsonProperty("Last Modified")
        @JsonDeserialize(using = NetSuiteDateDeserialiser.class)
        LocalDateTime lastModified,

        @JsonProperty("Date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        LocalDate date,

        @JsonProperty("Due Date/Receive By")
        @JsonDeserialize(using = NetSuiteDateDeserialiser.class)
        LocalDateTime dueDate,

        @JsonProperty("ID") String id,

        @JsonProperty("Company Name") String companyName,

        @JsonProperty("Period") String period,

        @JsonProperty("Tax Period") String taxPeriod,

        @JsonProperty("Tax Item") String taxItem,

        @JsonProperty("Cost Center (no hierarchy)") String costCenter,

        @JsonProperty("Internal ID") String internalID,

        @JsonProperty("Transaction Number") String transactionNumber,

        @JsonProperty("Document Number") String documentNumber,

        @JsonProperty("Number") String number,

        @JsonProperty("Name") String name,

        @JsonProperty("Tax Number") String taxNumber,

        @JsonProperty("Project (no hierarchy)") String project,

        @JsonProperty("Rate") String rate,

        @JsonProperty("Account (Main)") String accountMain,

        @JsonProperty("Memo") String memo,

        @JsonProperty("Memo (Main)") String memoMain,

        @JsonProperty("Currency") String currency,

        @JsonProperty("Exchange Rate") String exchangeRate,

        @JsonProperty("Amount (Debit) (Foreign Currency)") String amountDebitForeignCurrency,

        @JsonProperty("Amount (Credit) (Foreign Currency)") String amountCreditForeignCurrency,

        @JsonProperty("Amount (Debit)") String amountDebit,

        @JsonProperty("Amount (Credit)") String amountCredit,

        @JsonProperty("Intercompany") String intercompany,

        @JsonProperty("Status") String status,

        @JsonProperty("Approval History") String approvalHistory
) { }
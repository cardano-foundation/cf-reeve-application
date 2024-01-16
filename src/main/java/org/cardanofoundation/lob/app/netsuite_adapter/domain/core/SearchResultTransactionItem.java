package org.cardanofoundation.lob.app.netsuite_adapter.domain.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.*;
import org.cardanofoundation.lob.app.netsuite_adapter.util.NetSuiteDateDeserialiser;
import org.cardanofoundation.lob.app.netsuite_adapter.util.validation.EnumNamePattern;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)

// https://docs.google.com/spreadsheets/d/1iGo1t2bLuWSONOYo6kG9uXSzt7laCrM8gluKkx8tmn0/edit#gid=501685631
public record SearchResultTransactionItem(

        @JsonProperty("Line ID")
        @PositiveOrZero
        Integer lineID,

        @JsonProperty("Subsidiary (no hierarchy)")
        @Positive
        Integer subsidiary,

        @JsonProperty("Type")
        @EnumNamePattern(regexp =  "Journal|CardChrg|VendBill|CardRfnd|FxReval|Transfer|CustPymt")
        Type type,

        @JsonProperty("Date Created")
        @JsonDeserialize(using = NetSuiteDateDeserialiser.class)
        LocalDateTime dateCreated,

        @JsonProperty("ID")
        String id,

        @JsonProperty("Company Name")
        String companyName,

        @JsonProperty("Period")
        String period,

        @JsonProperty("Tax Item")
        String taxItem,

        @JsonProperty("Cost Center (no hierarchy)")
        @Nullable
        String costCenter,

        @JsonProperty("Transaction Number")
        @NotBlank
        String transactionNumber,

        @JsonProperty("Document Number")
        @Nullable
        String documentNumber,

        @JsonProperty("Number")
        String number,

        @JsonProperty("Name")
        @Nullable
        String name,

        @JsonProperty("Project (no hierarchy)")
        String project,

        @JsonProperty("Account (Main)")
        String accountMain,

        @JsonProperty("Memo (Main)")
        String memo,

        @JsonProperty("Currency")
        @Positive
        Integer currency,

        @NotNull
        @JsonProperty("Exchange Rate")
        @Positive
        BigDecimal exchangeRate,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("Amount (Debit) (Foreign Currency)") BigDecimal amountDebitForeignCurrency,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("Amount (Credit) (Foreign Currency)") BigDecimal amountCreditForeignCurrency,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("Amount (Debit)") BigDecimal amountDebit,

        @DecimalMin(value = "0.0")
        @Nullable
        @JsonProperty("Amount (Credit)") BigDecimal amountCredit

) { }

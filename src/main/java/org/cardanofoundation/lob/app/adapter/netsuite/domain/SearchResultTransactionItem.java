package org.cardanofoundation.lob.app.adapter.netsuite.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.cardanofoundation.lob.app.adapter.netsuite.util.NetSuiteDateDeserialiser;
import org.cardanofoundation.lob.app.adapter.netsuite.util.validation.EnumNamePattern;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)

// https://docs.google.com/spreadsheets/d/1iGo1t2bLuWSONOYo6kG9uXSzt7laCrM8gluKkx8tmn0/edit#gid=501685631
public record SearchResultTransactionItem(

        @JsonProperty("Subsidiary (no hierarchy)")
        @Positive
        Integer subsidiary,

        @JsonProperty("Type")
        @EnumNamePattern(regexp =  "Journal|CardChrg|VendorBill|CardRfnd|FxReval|Transfer|CustPymt")
        Type type,

        @JsonProperty("Date Created")
        @JsonDeserialize(using = NetSuiteDateDeserialiser.class)
        LocalDateTime dateCreated,

        @JsonProperty("ID")
        String id,

        //@NotBlank
        @JsonProperty("Company Name")
        String companyName,

        //@NotBlank
        @JsonProperty("Period")
        String period,

        //@NotBlank
        @JsonProperty("Tax Item")
        String taxItem,

        //@NotBlank
        @JsonProperty("Cost Center (no hierarchy)")
        String costCenter,

        @JsonProperty("Transaction Number")
        @NotBlank
        String transactionNumber,

        //@NotBlank
        @JsonProperty("Document Number")
        String documentNumber,

        @NotBlank
        @JsonProperty("Number")
        String number,

        //@NotBlank
        @JsonProperty("Name")
        String name,

        //@NotBlank
        @JsonProperty("Project (no hierarchy)")
        String project,

        //@NotBlank
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

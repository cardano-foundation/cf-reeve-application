package org.cardanofoundation.lob.sourceadapter.netsuite.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.constants.Constants;
import org.cardanofoundation.lob.common.crypto.Hashing;
import org.cardanofoundation.lob.common.model.LedgerEvent;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

@Data
@Log4j2
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkExportLedgerEvent {

    private final static Pattern COST_CENTER_EXTRACTOR_PATTERN = Pattern.compile("^\\d+");

    //@CsvBindByName(column = "Subsidiary: Name")
    @JsonProperty("Internal ID")
    private String subsidiaryName;

    @JsonProperty("Type")
    private String type;

    //@CsvBindByName(column = "Date")
    //@CsvDate("dd/MM/yyyy")
    @JsonProperty("Date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.METADATA_DATE_PATTERN)
    private Date date;

    //@CsvBindByName(column = "Accounting Period: Name")
    @JsonProperty("Period")
    private String accountingPeriodName;

    //@CsvBindByName(column = "Transaction Number")
    @JsonProperty("Transaction Number")
    private String transactionNumber;

    //@CsvBindByName(column = "Document Number")
    @JsonProperty("Document Number")
    private String documentNumber;

    @JsonProperty("Number")
    private String number;

    //@CsvBindByName(column = "Entity: ID")
    private String entityId;

    //@CsvBindByName(column = "Name")
    private String entityName;

    //@CsvBindByName(column = "Entity: Tax Reg. Number")
    private String entityTaxRegistrationNumber;

    @JsonProperty("Currency")
    private String currencySymbol;

    @JsonProperty("Amount (Debit) (Foreign Currency)")
    private Double amountDebitForeignCurrency;

    @JsonProperty("Amount (Debit)")
    private String amountDebit;

    @JsonProperty("Amount (Credit) (Foreign Currency)")
    private Double amountCreditForeignCurrency;

    @JsonProperty("Amount (Credit)")
    private String amountCredit;

    //@CsvBindByName(column = "Item: Taxable")
    private String itemTaxable;

    //@CsvBindByName(column = "Sales Tax Item: Tax Rate")
    private String salesTaxItemTaxRate;

    //@CsvBindByName(column = "Account (Line): Number")
    private String accountLineNumber;

    //@CsvBindByName(column = "Account (Line): Name")
    private String accountLineName;

    //@CsvBindByName(column = "Account: Number")
    private String accountNumber;

    //@CsvBindByName(column = "Account: Name")
    private String accountName;

    //@CsvBindByName(column = "Cost Center: Name")
    private String costCenterName;

    //@CsvBindByName(column = "Project: Name")
    private String projectName;

    //@CsvBindByName(column = "Memo")
    @JsonProperty("Memo")
    private String memo;

    @JsonProperty("Last Modified")
    private String lastModified;

    //@CsvBindByName(column = "Exchange Rate")
    @JsonProperty("Exchange Rate")
    private Double exchangeRate;

    @JsonProperty("Status")
    private String status;

    private static Optional<String> eventCodeFromDebitAndCredit(String debitAccountNumber, String creditAccountNumber) throws IllegalArgumentException {
        try {
            if (debitAccountNumber != null && creditAccountNumber != null) {
                final String debitAccount = debitAccountNumber.substring(0, 6);
                final String creditAccount = creditAccountNumber.substring(0, 6);
                return Optional.of(CodeComponentMapping.getEventCodeFromCodeComponents(
                        CodeComponentMapping.getCodeComponentFromAccount(debitAccount).orElseThrow(() -> new IllegalArgumentException(String.format("Could not find a matching value for debit account %s", debitAccount))),
                        CodeComponentMapping.getCodeComponentFromAccount(creditAccount).orElseThrow(() -> new IllegalArgumentException(String.format("Could not find a matching value for credit account %s", creditAccount)))
                ).orElseThrow(() -> new IllegalArgumentException(String.format("Could not find a matching event code %s %s", debitAccount, creditAccount))));
            } else {
                return Optional.empty();
            }
        } catch (final IllegalArgumentException e) {
            log.error(String.format("Could not parse event code from debit and credit account %s %s", debitAccountNumber, creditAccountNumber), e);
            return Optional.empty();
        }
    }

    private String computeFingerprint() {
        log.info(this.toString());
        return Hashing.blake2b256Hex(this.toString().getBytes(StandardCharsets.UTF_8));
    }

    public Optional<LedgerEvent> toLedgerEvent() {
        try {
            final LedgerEvent ledgerEvent = new LedgerEvent();
            ledgerEvent.setSourceEventFingerprint(computeFingerprint());
            ledgerEvent.setEntity(subsidiaryName);
            ledgerEvent.setModule("NS");
            ledgerEvent.setType(type);
            ledgerEvent.setDocDate(date);
            ledgerEvent.setBookDate(accountingPeriodName);
            ledgerEvent.setTransactionNumber(transactionNumber);
            ledgerEvent.setDocumentNumber(documentNumber);
            ledgerEvent.setEvent(eventCodeFromDebitAndCredit(accountLineNumber, accountNumber).orElse(null));
            ledgerEvent.setCurrency(currencySymbol);
            ledgerEvent.setNumber(number);
            ledgerEvent.setAmountDebit(amountDebitForeignCurrency);
            ledgerEvent.setAmountCredit(amountCreditForeignCurrency);
            ledgerEvent.setStatus(status);
            ledgerEvent.setExchangeRate(exchangeRate);
            ledgerEvent.setCounterParty(entityId);
            ledgerEvent.setProjectCode(projectName);
            ledgerEvent.setTimestamp(Instant.now().getEpochSecond());
            return Optional.of(ledgerEvent);
        } catch (final IllegalArgumentException e) {
            log.error("Could not parse bulk event to LedgerEvent.", e);
            return Optional.empty();
        }
    }
}

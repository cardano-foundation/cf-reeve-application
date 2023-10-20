package org.cardanofoundation.lob.sourceadapter.netsuite.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.constants.Constants;
import org.cardanofoundation.lob.common.crypto.Hashing;
import org.cardanofoundation.lob.common.model.LedgerEvent;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Data
@Log4j2
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkExportLedgerEvent {

    private final static Pattern COST_CENTER_EXTRACTOR_PATTERN = Pattern.compile("^\\d+");

    //@CsvBindByName(column = "Subsidiary: Name")
    @JsonProperty("Subsidiary (no hierarchy)")
    private String subsidiaryName;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Date Created")
    private String dateCreated;

    @JsonProperty("Last Modified")
    private String lastModified;

    //@CsvBindByName(column = "Date")
    //@CsvDate("dd/MM/yyyy")
    @JsonProperty("Date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.METADATA_DATE_PATTERN)
    private Date date;

    //@CsvBindByName(column = "Accounting Period: Name")
    @JsonProperty("Period")
    private String period;

    @JsonProperty("Tax Period")
    private String taxPeriod;

    @JsonProperty("Internal ID")
    private String internalID;

    //@CsvBindByName(column = "Transaction Number")
    @JsonProperty("Transaction Number")
    private String transactionNumber;

    //@CsvBindByName(column = "Document Number")
    @JsonProperty("Document Number")
    private String documentNumber;

    @JsonProperty("Number")
    private String number;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Tax Number")
    private String taxNumber;

    @JsonProperty("Project (no hierarchy)")
    private String project;

    @JsonProperty("Rate")
    private String rate;

    @JsonProperty("Account (Main)")
    private String account;

    @JsonProperty("Memo")
    private String memo;

    @JsonProperty("Memo (Main)")
    private String memoMain;

    @JsonProperty("Currency")
    private String currencySymbol;

    @JsonProperty("Exchange Rate")
    private String exchangeRate;

    @JsonProperty("Amount (Debit) (Foreign Currency)")
    private Double amountDebitForeignCurrency;

    @JsonProperty("Amount (Debit)")
    private String amountDebit;

    @JsonProperty("Amount (Credit) (Foreign Currency)")
    private Double amountCreditForeignCurrency;

    @JsonProperty("Amount (Credit)")
    private String amountCredit;

    @JsonProperty("Intercompany")
    private String intercompany;

    @JsonProperty("Status")
    private String status;


    @JsonProperty("Due Date/Receive By")
    private String dueDateReceiveBy;

    private static Optional<String> eventCodeFromDebitAndCredit(String debitAccountNumber, String creditAccountNumber) throws IllegalArgumentException {
        try {
            if (debitAccountNumber != null && creditAccountNumber != null) {
                final String debitAccount = debitAccountNumber.substring(0, 6);
                final String creditAccount = creditAccountNumber.substring(0, 6);
                return Optional.of(CodeComponentMapping.getEventCodeFromCodeComponents(
                        CodeComponentMapping.getCodeComponentFromAccount(debitAccount).orElseThrow(() -> new IllegalArgumentException(String.format("Could not find a matching value for debit account %s", debitAccount))),
                        CodeComponentMapping.getCodeComponentFromAccount(creditAccount).orElseThrow(() -> new IllegalArgumentException(String.format("Could not find a matching value for credit account %s", creditAccount)))
                ).orElseThrow(() -> new IllegalArgumentException(String.format("Could not find a matching event code %s %s", debitAccount, creditAccount))));
            }

        } catch (final IllegalArgumentException e) {
            log.error(String.format("Could not parse event code from debit and credit account %s %s", debitAccountNumber, creditAccountNumber), e);
            return Optional.empty();
        }
        return Optional.empty();
    }

    private String computeFingerprint() {
        log.info(this.toString());

        return Hashing.blake2b256Hex((this.toString()).getBytes(StandardCharsets.UTF_8));
    }

    public Optional<LedgerEvent> toLedgerEvent() {
        try {
            final LedgerEvent ledgerEvent = new LedgerEvent();
            ledgerEvent.setSourceEventFingerprint(computeFingerprint());
            ledgerEvent.setEntity(subsidiaryName);
            ledgerEvent.setModule("NS");
            ledgerEvent.setType(type);
            ledgerEvent.setDocDate(date);
            ledgerEvent.setPeriod(period);
            //ledgerEvent.setTransactionNumber(transactionNumber);
            ledgerEvent.setLineNumber(documentNumber);
            ledgerEvent.setEventCode(eventCodeFromDebitAndCredit(null,null).orElse(null));
            ledgerEvent.setCurrency(currencySymbol);
            ledgerEvent.setLineNumber(number);
            ledgerEvent.setAmount(!amountDebit.isBlank() ? amountDebit : amountCredit);

            ledgerEvent.setStatus(status);
            ledgerEvent.setExchangeRate(exchangeRate);
            ledgerEvent.setEntity(internalID);
            ledgerEvent.setProjectCode("projectName");
            ledgerEvent.setTimestamp(Instant.now().getEpochSecond());
            return Optional.of(ledgerEvent);
        } catch (final IllegalArgumentException e) {
            log.error("Could not parse bulk event to LedgerEvent.", e);
            return Optional.empty();
        }
    }
}

package org.cardanofoundation.lob.sourceadapter.netsuite.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.jcajce.provider.digest.Blake2b;
import org.cardanofoundation.lob.common.crypto.Hashing;
import org.cardanofoundation.lob.common.model.LedgerEvent;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Log4j2
public class BulkExportLedgerEvent {

    private final static Pattern COST_CENTER_EXTRACTOR_PATTERN = Pattern.compile("^\\d+");

    @CsvBindByName(column = "Subsidiary: Name")
    private String subsidiaryName;

    @CsvBindByName(column = "Type")
    private String type;

    @CsvBindByName(column = "Date")
    @CsvDate("dd/MM/yyyy")
    private Date date;

    @CsvBindByName(column = "Accounting Period: Name")
    private String accountingPeriodName;

    @CsvBindByName(column = "Transaction Number")
    private String transactionNumber;

    @CsvBindByName(column = "Document Number")
    private String documentNumber;

    @CsvBindByName(column = "Entity: ID")
    private String entityId;

    @CsvBindByName(column = "Name")
    private String entityName;

    @CsvBindByName(column = "Entity: Tax Reg. Number")
    private String entityTaxRegistrationNumber;

    @CsvBindByName(column = "Currency: Currency Symbol")
    private String currencySymbol;

    @CsvBindByName(column = "Amount (Foreign Currency)", locale = "en-US")
    private Double amountForeignCurrency;

    @CsvBindByName(column = "Amount")
    private String amount;

    @CsvBindByName(column = "Item: Taxable")
    private String itemTaxable;

    @CsvBindByName(column = "Sales Tax Item: Tax Rate")
    private String salesTaxItemTaxRate;

    @CsvBindByName(column = "Account (Line): Number")
    private String accountLineNumber;

    @CsvBindByName(column = "Account (Line): Name")
    private String accountLineName;

    @CsvBindByName(column = "Account: Number")
    private String accountNumber;

    @CsvBindByName(column = "Account: Name")
    private String accountName;

    @CsvBindByName(column = "Cost Center: Name")
    private String costCenterName;

    @CsvBindByName(column = "Project: Name")
    private String projectName;

    @CsvBindByName(column = "Memo")
    private String memo;

    @CsvBindByName(column = "Exchange Rate")
    private Double exchangeRate;

    private static Optional<String> extractCostCenterNumber(final String costCenterName) {
        final Matcher matcher = COST_CENTER_EXTRACTOR_PATTERN.matcher(costCenterName.trim());
        if (matcher.find()) {
            return Optional.of(matcher.group());
        } else {
            return Optional.empty();
        }
    }

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
        final StringWriter stringWriter = new StringWriter();

        stringWriter.append("subsidiaryName:");
        stringWriter.append(subsidiaryName);
        stringWriter.append(";");

        stringWriter.append("type:");
        stringWriter.append(type);
        stringWriter.append(";");

        stringWriter.append("date:");
        stringWriter.append(date.toString());
        stringWriter.append(";");

        stringWriter.append("accountingPeriodName:");
        stringWriter.append(accountingPeriodName);
        stringWriter.append(";");

        stringWriter.append("transactionNumber:");
        stringWriter.append(transactionNumber);
        stringWriter.append(";");

        stringWriter.append("documentNumber:");
        stringWriter.append(documentNumber);
        stringWriter.append(";");

        stringWriter.append("entityId:");
        stringWriter.append(entityId);
        stringWriter.append(";");

        stringWriter.append("entityName:");
        stringWriter.append(entityName);
        stringWriter.append(";");

        stringWriter.append("entityTaxRegistrationNumber:");
        stringWriter.append(entityTaxRegistrationNumber);
        stringWriter.append(";");

        stringWriter.append("currencySymbol:");
        stringWriter.append(currencySymbol);
        stringWriter.append(";");

        stringWriter.append("amountForeignCurrency:");
        stringWriter.append(amountForeignCurrency.toString());
        stringWriter.append(";");

        stringWriter.append("amount:");
        stringWriter.append(amount);
        stringWriter.append(";");

        stringWriter.append("itemTaxable:");
        stringWriter.append(itemTaxable);
        stringWriter.append(";");

        stringWriter.append("salesTaxItemTaxRate:");
        stringWriter.append(salesTaxItemTaxRate);
        stringWriter.append(";");

        stringWriter.append("accountLineNumber:");
        stringWriter.append(accountLineNumber);
        stringWriter.append(";");

        stringWriter.append("accountLineName:");
        stringWriter.append(accountLineName);
        stringWriter.append(";");

        stringWriter.append("accountNumber:");
        stringWriter.append(accountNumber);
        stringWriter.append(";");

        stringWriter.append("accountName:");
        stringWriter.append(accountName);
        stringWriter.append(";");

        stringWriter.append("costCenterName:");
        stringWriter.append(costCenterName);
        stringWriter.append(";");

        stringWriter.append("projectName:");
        stringWriter.append(projectName);
        stringWriter.append(";");

        stringWriter.append("memo:");
        stringWriter.append(memo);
        stringWriter.append(";");

        stringWriter.append("exchangeRate:");
        stringWriter.append(exchangeRate.toString());
        stringWriter.append(";");

        return Hashing.blake2b256Hex(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
    }

    public Optional<LedgerEvent> toLedgerEvent() {
        try {
            final LedgerEvent ledgerEvent = new LedgerEvent();
            ledgerEvent.setSourceEventFingerprint(computeFingerprint());
            ledgerEvent.setEntity(subsidiaryName);
            ledgerEvent.setModule("NS");
            ledgerEvent.setType("A");
            ledgerEvent.setDocDate(date);
            ledgerEvent.setBookDate(accountingPeriodName);
            ledgerEvent.setTransactionNumber(transactionNumber);
            ledgerEvent.setDocumentNumber(documentNumber);
            ledgerEvent.setEvent(eventCodeFromDebitAndCredit(accountLineNumber, accountNumber).orElse(null));
            ledgerEvent.setCurrency(currencySymbol);
            ledgerEvent.setAmount(amountForeignCurrency);
            ledgerEvent.setExchangeRate(exchangeRate);
            ledgerEvent.setCounterParty(entityId);
            ledgerEvent.setCostCenter(extractCostCenterNumber(costCenterName).orElse(null));
            ledgerEvent.setProjectCode(projectName);
            ledgerEvent.setTimestamp(Date.from(Instant.now()));
            return Optional.of(ledgerEvent);
        } catch (final IllegalArgumentException e) {
            log.error("Could not parse bulk event to LedgerEvent.", e);
            return Optional.empty();
        }
    }
}

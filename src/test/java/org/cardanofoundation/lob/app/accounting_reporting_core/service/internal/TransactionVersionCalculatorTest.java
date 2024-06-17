package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type.VENDOR;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionVersionAlgo.ERP;

@Slf4j
class TransactionVersionCalculatorTest {

    @Test
    public void compute() {
        val org = Organisation.builder()
                .id("401cad588bb2152f5c7ea0646ed84dd7f1b233dc73c3463d721f43e117a0e8ad")
                .build();

        val t = new TransactionEntity();
        t.setTransactionType(FxRevaluation);
        t.setEntryDate(LocalDate.of(2021, 1, 1));
        t.setOrganisation(org);
        t.setTransactionInternalNumber("FxRevaluation-1");
        t.setAccountingPeriod(YearMonth.of(2021, 1));

        val txItem1 = new TransactionItemEntity();
        txItem1.setId("1");
        txItem1.setAmountFcy(BigDecimal.valueOf(100.10));
        txItem1.setAccountDebit(Account.builder()
                .code("1000")
                .name("Cash")
                .refCode("r1000")
                .build()
        );

        txItem1.setAccountCredit(Account.builder()
                .code("2000")
                .name("Bank")
                .refCode("r2000")
                .build()
        );

        txItem1.setDocument(Document.builder()
                        .num("doc-1")
                        .vat(Vat.builder()
                                .customerCode("C100")
                                .build())
                        .currency(Currency.builder()
                                .id("ISO_4217:CHF")
                                .customerCode("CHF")
                                .build())
                        .counterparty(Counterparty.builder()
                                .customerCode("C100")
                                .name("Vendor Name")
                                .type(VENDOR)
                                .build())
                .build());

        t.setItems(Set.of(txItem1));

        val tHash = TransactionVersionCalculator.compute(ERP, t);

        assertThat(tHash).isNotNull();

        assertThat(tHash).isEqualTo("064404f9af7b626bbb98373f143a75c929a30cd20d84c4f9bd559ce68e246a66");
    }

}

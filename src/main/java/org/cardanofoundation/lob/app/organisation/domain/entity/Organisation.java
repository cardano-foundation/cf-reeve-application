package org.cardanofoundation.lob.app.organisation.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity(name = "organisation")
public class Organisation {

    @Id
    @Column(name = "organisation_id", nullable = false)
    private String id;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    @Column(name = "long_name", nullable = false)
    private String longName;

    @Column(name = "pre_approve_transactions")
    private Boolean preApproveTransactions;

    @Column(name = "pre_approve_transactions_dispatch")
    private Boolean preApproveTransactionsDispatch;

    @Column(name = "vat_number", nullable = false)
    private String vatNumber;

    @Column(name = "accounting_period_months", nullable = false)
    private int accountPeriodMonths;

    @Column(name = "currency_id", nullable = false)
    private String currencyId;

    public static String id(String vatId) {
        return digestAsHex(vatId);
    }

    public boolean isPreApproveTransactionsEnabled() {
        return Optional.ofNullable(preApproveTransactions).orElse(false);
    }

    public boolean isPreApproveTransactionsDispatchEnabled() {
        return Optional.ofNullable(preApproveTransactionsDispatch).orElse(false);
    }

}

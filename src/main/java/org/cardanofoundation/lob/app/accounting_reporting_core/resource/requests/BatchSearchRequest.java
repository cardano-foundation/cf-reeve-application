package org.cardanofoundation.lob.app.accounting_reporting_core.resource.requests;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionBatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;


import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatchSearchRequest {

    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    private String organisationId;

    @Schema(example = "[\"CREATED\",\"PROCESSING\"]")
    private Set<TransactionBatchStatus> status = Set.of();

    @Schema(example = "[\"OK\",\"FAIL\"]")
    private Set<TransactionStatus> txStatus = Set.of();

    @Schema(example = "[\"CardCharge\",\"VendorBill\",\"CardRefund\",\"Journal\",\"FxRevaluation\",\"Transfer\",\"CustomerPayment\",\"ExpenseReport\",\"VendorPayment\",\"BillCredit\"]")
    private Set<TransactionType> transactionTypes = Set.of();

    @Schema(example = "2014-01")
    @Nullable
    private YearMonth accountingPeriodFrom;

    @Nullable
    @Schema(example = "2024-01")
    private YearMonth accountingPeriodTo;

}

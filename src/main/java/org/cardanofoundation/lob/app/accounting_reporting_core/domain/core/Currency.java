package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class Currency {

    private String customerCode;

    @Builder.Default
    private Optional<CoreCurrency> coreCurrency = Optional.empty();


}

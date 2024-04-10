package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReIngestionIntents {

    @Builder.Default
    private ReprocessType reprocessType = ReprocessType.ONLY_FAILED;

    public static ReIngestionIntents newIngestion(ReprocessType reprocessType) {
        return ReIngestionIntents.builder()
                .reprocessType(reprocessType)
                .build();
    }

    public enum ReprocessType {
        FULL,
        ONLY_FAILED
    }

}

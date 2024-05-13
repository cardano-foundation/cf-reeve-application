package org.cardanofoundation.lob.app.organisation.domain.view;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrganisationView {
    @Schema(example = "75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
    String id;
    @Schema(example = "Cardano Foundation")
    String name;
    @Schema(example = "Description")
    String description;
    @Schema(example = "Currency Id")
    String currencyId;
    @Schema(example = "Accounting Period")
    Integer accountPeriodMonths;
}

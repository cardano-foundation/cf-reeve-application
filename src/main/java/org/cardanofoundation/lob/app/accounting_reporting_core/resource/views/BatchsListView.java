package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Rename the class name BatchView
 */
@Getter
@Setter
@AllArgsConstructor
public class BatchsListView {
    private String id;
    private String createdAt;
    private String updatedAt;
    private String organisationId;
}

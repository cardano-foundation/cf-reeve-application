package org.cardanofoundation.lob.app.organisation.domain.core;

public record CharterOfAccounts(String code,
                                String refCode,
                                ERPDataSource erpDataSource,
                                String internalNumber) {
}

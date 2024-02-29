package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.core.CharterOfAccounts;
import org.cardanofoundation.lob.app.organisation.domain.core.ERPDataSource;

import java.util.Optional;

public interface ChartOfAccountsRepository {

    Optional<CharterOfAccounts> getCharterAccount(String accountCode);

    Optional<CharterOfAccounts> getCharterAccount(ERPDataSource erpDataSource, String internalNumber);

}

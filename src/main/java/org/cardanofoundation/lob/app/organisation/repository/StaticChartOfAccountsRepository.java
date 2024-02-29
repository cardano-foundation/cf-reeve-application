package org.cardanofoundation.lob.app.organisation.repository;

import lombok.val;
import org.cardanofoundation.lob.app.organisation.domain.core.CharterOfAccounts;
import org.cardanofoundation.lob.app.organisation.domain.core.ERPDataSource;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.cardanofoundation.lob.app.organisation.domain.core.ERPDataSource.NETSUITE;

@Service
public class StaticChartOfAccountsRepository implements ChartOfAccountsRepository {

    @Override
    public Optional<CharterOfAccounts> getCharterAccount(String accountCode) {
        val internalNumber = String.valueOf((int) Math.floor((double) Long.parseLong(accountCode) / 2));

        return Optional.of(new CharterOfAccounts(accountCode, STR."R:\{accountCode}", NETSUITE, internalNumber));
    }

    @Override
    public Optional<CharterOfAccounts> getCharterAccount(ERPDataSource erpDataSource, String internalNumber) {
        String code = String.valueOf(Long.parseLong(internalNumber) * 2);

        return Optional.of(new CharterOfAccounts(STR."\{code}\{internalNumber}", STR."R:\{code}", erpDataSource, internalNumber));
    }

}

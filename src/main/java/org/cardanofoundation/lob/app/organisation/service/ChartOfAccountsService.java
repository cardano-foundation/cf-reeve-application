package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.CharterOfAccounts;
import org.cardanofoundation.lob.app.organisation.domain.core.ERPDataSource;
import org.cardanofoundation.lob.app.organisation.repository.ChartOfAccountsRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChartOfAccountsService {

    private final ChartOfAccountsRepository eventCodeMappingRepository;

    public Optional<CharterOfAccounts> getChartAccount(String accountCode) {
        return eventCodeMappingRepository.getCharterAccount(accountCode);
    }

    public Optional<CharterOfAccounts> getChartAccount(ERPDataSource erpDataSource, String internalNumber) {
        return eventCodeMappingRepository.getCharterAccount(erpDataSource, internalNumber);
    }

}

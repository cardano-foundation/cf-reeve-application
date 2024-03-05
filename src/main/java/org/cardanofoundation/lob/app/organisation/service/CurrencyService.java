package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.Currency;
import org.cardanofoundation.lob.app.organisation.repository.CurrencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    //@Transactional(readOnly = true)
    public Optional<Currency> findByCurrencyId(String currencyId) {
        return currencyRepository.findByCurrencyId(currencyId);
    }

    public List<Currency> listAll() {
        return currencyRepository.allCurrencies();
    }

}

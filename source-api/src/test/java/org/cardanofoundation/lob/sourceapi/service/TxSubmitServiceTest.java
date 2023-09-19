package org.cardanofoundation.lob.sourceapi.service;

import org.cardanofoundation.lob.common.model.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class TxSubmitServiceTest {


    @Disabled
    @Test
    void submitTxTest() {
        final List<LedgerEvent> ledgerEvents = new ArrayList<>();
        final LedgerEvent ledgerEvent = new LedgerEvent();
        ledgerEvents.add(ledgerEvent);
        final LedgerEventRegistrationJob registrationJob = new LedgerEventRegistrationJob();
        registrationJob.setRegistrationId("12345");
        registrationJob.setLedgerEvents(ledgerEvents);
        registrationJob.setJobStatus(LedgerEventRegistrationJobStatus.APPROVED);

    }
}

package org.cardanofoundation.lob.app.netsuite_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.Violation;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.Set;

import static org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity.ERROR;

@Service("netsuite.NotificationsSenderService")
@Slf4j
@RequiredArgsConstructor
public class NotificationsSenderService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void sendNotifications(Set<Violation> violations) {
        if (violations.isEmpty()) {
            return;
        }

        log.info(STR."Sending notifications for \{violations.size()} violations.");

        for (val violation : violations) {
            val problemBuilder = Problem.builder()
                    .withTitle(STR."NETSUITE:\{violation.getCode()}")
                    .withDetail(STR."NetSuite Adapter Error, code: \{violation.getCode()}")
                    .with("module", "NETSUITE")
                    .with("code", violation.getCode())
                    .with("subsidiary", violation.getSubsidiary())
                    .with("transactionNumber", violation.getTransactionNumber())
                    .with("lineId", violation.getLineId()
            );

            violation.getBag().forEach((k, v) -> problemBuilder.with(STR."p_\{k}", v));

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, problemBuilder.build()));
        }
    }

}


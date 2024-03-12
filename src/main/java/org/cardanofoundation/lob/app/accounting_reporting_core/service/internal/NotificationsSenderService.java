package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import java.util.Set;

import static org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity.ERROR;

@Service("accounting_reporting_core.NotificationsSenderService")
@Slf4j
@RequiredArgsConstructor
public class NotificationsSenderService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void sendNotifications(Set<Violation> violations) {
        for (val violation : violations) {
            if (violation.type() == Violation.Type.ERROR) {
                val problemBuilder = Problem.builder()
                        .withTitle(STR."ACCOUNTING_CORE:\{violation.code()}")
                        .withDetail(STR."Accounting Business Rule Error, code: \{violation.code()}")
                        .with("processorModule", violation.processorModule())
                        .with("module", "ACCOUNTING_CORE")
                        .with("organisationId", violation.organisationId())
                        .with("transactionId", violation.transactionId()
                        );

                violation.txItemId().ifPresent(txItemId -> problemBuilder.with("txItemId", txItemId));

                violation.bag().forEach((k, v) -> problemBuilder.with(STR."p_\{k}", v));

                applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, problemBuilder.build()));
            }
        }
    }
}

package org.cardanofoundation.lob.app.accounting_reporting_core.service;

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

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationViolationHandler {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void sendViolationNotifications(Set<Violation> violations) {
        log.info("Sending violations notification..., count:{}", violations.size());

        // TODO this is quite clunky, perhaps we need to translate this better

        for (val violation : violations) {
            val issue = Problem.builder()
                    .withTitle("ACCOUNTING_CORE::ACCOUNTING_RULES_ERROR")
                    .withDetail(STR."Violation of an accounting business rule {\{violation.violationCode()}}, transactionNumber: \{violation.transactionId()}, txLine: \{violation.txItemId().orElse("unknown")}")
                    .build();

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, issue));
        }
    }

}

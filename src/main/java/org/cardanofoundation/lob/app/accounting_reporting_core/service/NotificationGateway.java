package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationGateway {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void sendViolationNotifications(Set<Violation> violations) {
        log.info("Sending violation notification..., count:{}", violations.size());

        // TODO this is quite clunky, perhaps we need to translate this better

        violations.forEach(violation -> {
            applicationEventPublisher.publishEvent(NotificationEvent.create(
                    NotificationSeverity.ERROR,
                    STR."VIOLATION_ \{violation.violationCode()}",
                    "Business Rule Violation",
                    STR."Violation of Business Rule \{violation.violationCode()}",
                    Map.of("violationParams", violation.bag())
            ));
        });
    }

}

package org.cardanofoundation.lob.app.notification_gateway.domain.event;

import org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity;
import org.springframework.modulith.events.Externalized;
import org.zalando.problem.Problem;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Event responsible for notifying the LOB user of a problem
 *
 * @param id
 * @param severity
 * @param message
 * @param problem
 */
@Externalized("target")
// TODO add organisationId and creationDate, expirationDate, acked notification?
public record NotificationEvent(UUID id,
                                String code,
                                NotificationSeverity severity,
                                String title,
                                String message,
                                Optional<Problem> problem,
                                Map<String, Object> bag
                                ) {

  public static NotificationEvent create(NotificationSeverity severity,
                                         String code,
                                         String title,
                                         String message,
                                         @Nullable Problem problem,
                                         Map<String, Object> bag) {
    return new NotificationEvent(
            UUID.randomUUID(),
            code,
            severity,
            title,
            message,
            Optional.ofNullable(problem),
            bag
    );
  }

  public static NotificationEvent create(NotificationSeverity severity,
                                         String code,
                                         String title,
                                         String message,
                                         @Nullable Problem problem) {
    return new NotificationEvent(
            UUID.randomUUID(),
            code,
            severity,
            title,
            message,
            Optional.ofNullable(problem),
            Map.of()
    );
  }

  public static NotificationEvent create(NotificationSeverity severity,
                                         String code,
                                         String title,
                                         String message,
                                         Map<String, Object> bag) {
    return new NotificationEvent(
            UUID.randomUUID(),
            code,
            severity,
            title,
            message,
    Optional.empty(),
            bag);
  }

  public static NotificationEvent create(NotificationSeverity severity,
                                         String code,
                                         String title,
                                         String message) {
    return new NotificationEvent(
            UUID.randomUUID(),
            code,
            severity,
            title,
            message,
            Optional.empty(),
            Map.of());
  }

}

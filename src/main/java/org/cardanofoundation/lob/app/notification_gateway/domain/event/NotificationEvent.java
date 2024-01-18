package org.cardanofoundation.lob.app.notification_gateway.domain.event;

import org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity;
import org.zalando.problem.Problem;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

//@Externalized("target")
public record NotificationEvent(UUID id,

                                NotificationSeverity severity,
                                String message,
                                Optional<Problem> problem
                                ) {

  public static NotificationEvent create(NotificationSeverity severity, String message, @Nullable Problem problem) {
    return new NotificationEvent(UUID.randomUUID(), severity, message, Optional.ofNullable(problem));
  }

  public static NotificationEvent create(NotificationSeverity severity, String message) {
    return new NotificationEvent(UUID.randomUUID(), severity, message, Optional.empty());
  }

}


package org.cardanofoundation.lob.app.notification.domain;

import org.springframework.modulith.events.Externalized;

import java.util.UUID;

@Externalized("target")
public record NotificationEvent(UUID id,
                                NotificationSeverity severity,
                                String message
                                ) {

  public static NotificationEvent create(NotificationSeverity severity, String message) {
    return new NotificationEvent(UUID.randomUUID(), severity, message);
  }

    public enum NotificationSeverity {
        INFO, WARN, ERROR
    }

}


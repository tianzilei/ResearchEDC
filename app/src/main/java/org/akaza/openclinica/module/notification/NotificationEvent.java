package org.akaza.openclinica.module.notification;

import java.time.Instant;
import org.springframework.context.ApplicationEvent;

/**
 * Application event published when a notification needs to be sent.
 * Cross-module communication via Spring Modulith events.
 */
public class NotificationEvent extends ApplicationEvent {

  private final String to;
  private final String subject;
  private final String body;
  private final NotificationType type;
  private final Instant occurredAt;

  public enum NotificationType {
    EMAIL,
    SYSTEM_ALERT
  }

  public NotificationEvent(Object source, String to, String subject, String body, NotificationType type) {
    super(source);
    this.to = to;
    this.subject = subject;
    this.body = body;
    this.type = type;
    this.occurredAt = Instant.now();
  }

  public String getTo() { return to; }
  public String getSubject() { return subject; }
  public String getBody() { return body; }
  public NotificationType getType() { return type; }
  public Instant getOccurredAt() { return occurredAt; }
}

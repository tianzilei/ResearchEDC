package org.akaza.openclinica.module.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for {@link NotificationEvent} published via Spring Modulith.
 *
 * Routes events to the appropriate {@link NotificationService}
 * implementation based on notification type.
 */
@Component
public class NotificationEventHandler {

  private static final Logger log = LoggerFactory.getLogger(NotificationEventHandler.class);

  private final EmailNotificationService emailService;

  public NotificationEventHandler(EmailNotificationService emailService) {
    this.emailService = emailService;
  }

  @EventListener
  public void handleNotification(NotificationEvent event) {
    log.info("Processing notification event: type={} to={}", event.getType(), event.getTo());

    switch (event.getType()) {
      case EMAIL -> emailService.send(event.getTo(), event.getSubject(), event.getBody());
      case SYSTEM_ALERT -> log.warn("System alert notification type not yet implemented");
    }
  }
}

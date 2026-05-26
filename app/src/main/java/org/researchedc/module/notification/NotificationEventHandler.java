package org.researchedc.module.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for {@link NotificationEvent} published via Spring Modulith.
 *
 * Currently logs all notifications. Email delivery has been disabled.
 */
@Component
public class NotificationEventHandler {

  private static final Logger log = LoggerFactory.getLogger(NotificationEventHandler.class);

  @EventListener
  public void handleNotification(NotificationEvent event) {
    log.info("Notification event: type={} to={} subject={}", event.getType(), event.getTo(), event.getSubject());
  }
}

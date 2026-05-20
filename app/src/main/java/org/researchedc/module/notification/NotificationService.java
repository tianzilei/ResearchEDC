package org.researchedc.module.notification;

/**
 * Service interface for sending notifications.
 * Implementations handle delivery (email, system alert, etc.).
 */
public interface NotificationService {

  /**
   * Send a notification.
   *
   * @param to      recipient address (email, username)
   * @param subject notification subject
   * @param body    notification body content
   */
  void send(String to, String subject, String body);
}

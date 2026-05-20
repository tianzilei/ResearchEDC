package org.researchedc.module.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Email-based notification service.
 *
 * Delegates to the existing {@code JavaMailSenderImpl} from the legacy
 * email configuration and will be gradually merged with
 * {@code BulkEmailSenderService} in subsequent iterations.
 */
@Service
public class EmailNotificationService implements NotificationService {

  private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

  private final JavaMailSenderImpl mailSender;

  public EmailNotificationService(@Autowired JavaMailSenderImpl mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void send(String to, String subject, String body) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(body, true);
      mailSender.send(message);
      log.info("Email sent to {}: {}", to, subject);
    } catch (MessagingException e) {
      log.error("Failed to send email to {}: {}", to, e.getMessage());
      throw new RuntimeException("Email delivery failed", e);
    }
  }
}

package org.akaza.openclinica.config;

import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Replaces applicationContext-core-email.xml.
 *
 * <p>Configures {@link JavaMailSenderImpl} using {@code spring.mail.*} properties
 * from {@code application.yml}, and exposes the legacy {@link OpenClinicaMailSender}
 * wrapper bean for backward compatibility with {@code XsltTransformJob} and other
 * callers that look up {@code "openClinicaMailSender"} by name.</p>
 */
@Configuration
public class MailConfig {

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.protocol:smtp}")
    private String protocol;

    @Value("${spring.mail.properties.mail.smtp.auth:false}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private String starttlsEnable;

    @Value("${spring.mail.properties.mail.smtps.auth:false}")
    private String smtpsAuth;

    @Value("${spring.mail.properties.mail.smtps.starttls.enable:false}")
    private String smtpsStarttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.connectiontimeout:5000}")
    private String connectionTimeout;

    @Value("${spring.mail.properties.mail.smtp.starttls.required:false}")
    private String starttlsRequired;

    @Value("${spring.mail.properties.mail.smtp.ssl.protocols:TLSv1.2}")
    private String sslProtocols;

    /**
     * Creates the primary {@link JavaMailSenderImpl} bean, replacing the XML-defined
     * {@code mailSender} bean. All properties are externalized to {@code application.yml}
     * under the {@code spring.mail.*} namespace.
     */
    @Bean("mailSender")
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setProtocol(protocol);
        mailSender.setDefaultEncoding("UTF-8");

        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.auth", smtpAuth);
        javaMailProperties.setProperty("mail.smtp.starttls.enable", starttlsEnable);
        javaMailProperties.setProperty("mail.smtps.auth", smtpsAuth);
        javaMailProperties.setProperty("mail.smtps.starttls.enable", smtpsStarttlsEnable);
        javaMailProperties.setProperty("mail.smtp.connectiontimeout", connectionTimeout);
        javaMailProperties.setProperty("mail.smtp.starttls.required", starttlsRequired);
        javaMailProperties.setProperty("mail.smtp.ssl.protocols", sslProtocols);
        mailSender.setJavaMailProperties(javaMailProperties);

        return mailSender;
    }

    /**
     * Creates the legacy {@link OpenClinicaMailSender} wrapper bean, replacing the
     * XML-defined {@code openClinicaMailSender} bean. Maintains backward compatibility
     * with code that accesses this bean by name (e.g., {@code XsltTransformJob}).
     */
    @Bean("openClinicaMailSender")
    public OpenClinicaMailSender openClinicaMailSender(JavaMailSenderImpl mailSender) {
        OpenClinicaMailSender sender = new OpenClinicaMailSender();
        sender.setMailSender(mailSender);
        return sender;
    }
}

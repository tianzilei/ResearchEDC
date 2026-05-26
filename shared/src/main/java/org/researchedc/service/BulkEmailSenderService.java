package org.researchedc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

/**
 * Mail service disabled — no mail server required.
 */
@Service
public class BulkEmailSenderService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public static void addMimeMessage(MimeMessagePreparator mimeMessage) {
        // Mail service disabled — message discarded
    }

}

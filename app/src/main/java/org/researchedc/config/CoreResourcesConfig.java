package org.researchedc.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class CoreResourcesConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasenames(
            "org.researchedc.i18n.admin",
            "org.researchedc.i18n.audit_events",
            "org.researchedc.i18n.exceptions",
            "org.researchedc.i18n.format",
            "org.researchedc.i18n.notes",
            "org.researchedc.i18n.page_messages",
            "org.researchedc.i18n.terms",
            "org.researchedc.i18n.words",
            "org.researchedc.i18n.workflow",
            "org.researchedc.i18n.ws_messages"
        );
        ms.setDefaultEncoding("UTF-8");
        return ms;
    }

    @Bean
    public SessionLocaleResolver localeResolver() {
        return new SessionLocaleResolver();
    }
}

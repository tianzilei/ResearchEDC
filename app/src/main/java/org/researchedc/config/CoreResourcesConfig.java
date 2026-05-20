package org.researchedc.config;

import java.util.Properties;
import org.researchedc.dao.core.CoreResources;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@Configuration
public class CoreResourcesConfig {

    @Bean
    public Properties dataInfo() {
        Properties props = new Properties();
        try {
            props.load(new ClassPathResource("datainfo.properties").getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load datainfo.properties", e);
        }
        return props;
    }

    @Bean
    public Properties extractInfo() {
        Properties props = new Properties();
        try {
            props.load(new ClassPathResource("extract.properties").getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load extract.properties", e);
        }
        return props;
    }

    @Bean
    public Properties enterpriseInfo() {
        Properties props = new Properties();
        try {
            props.load(new ClassPathResource("enterprise.properties").getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load enterprise.properties", e);
        }
        return props;
    }

    @Bean
    public CoreResources coreResources(Properties dataInfo, Properties extractInfo) {
        CoreResources cr = new CoreResources();
        cr.setDataInfo(dataInfo);
        cr.setExtractInfo(extractInfo);
        return cr;
    }

    @Bean
    public Properties dataInfoProperties(CoreResources coreResources) {
        return coreResources.getDataInfo();
    }

    @Bean
    public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer(
            Properties dataInfoProperties) {
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        ppc.setPlaceholderPrefix("s[");
        ppc.setPlaceholderSuffix("]");
        ppc.setProperties(dataInfoProperties);
        return ppc;
    }

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
        return ms;
    }

    @Bean
    public SessionLocaleResolver localeResolver() {
        return new SessionLocaleResolver();
    }
}

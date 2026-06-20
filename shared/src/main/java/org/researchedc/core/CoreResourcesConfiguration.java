package org.researchedc.core;

import java.io.InputStream;
import java.util.Properties;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreResourcesConfiguration {

    @Bean
    public Properties dataInfo() {
        return loadProperties("datainfo.properties");
    }

    @Bean
    public Properties extractInfo() {
        return loadProperties("extract.properties");
    }

    @Bean
    public CoreResources coreResources(Properties dataInfo, Properties extractInfo) {
        CoreResources coreResources = new CoreResources();
        coreResources.setDataInfo(dataInfo);
        coreResources.setExtractInfo(extractInfo);
        return coreResources;
    }

    @Bean
    public Properties dataInfoProperties(CoreResources coreResources) {
        return coreResources.getDataInfo();
    }

    @Bean
    public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer(Properties dataInfoProperties) {
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setPlaceholderPrefix("s[");
        configurer.setPlaceholderSuffix("]");
        configurer.setProperties(dataInfoProperties);
        return configurer;
    }

    private static Properties loadProperties(String resourceName) {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourceName)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
            return properties;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + resourceName, e);
        }
    }
}

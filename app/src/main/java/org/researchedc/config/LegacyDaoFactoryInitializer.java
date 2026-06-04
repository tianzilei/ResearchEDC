package org.researchedc.config;

import org.researchedc.dao.LegacyDaoFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Bridges the static LegacyDaoFactory to the Spring ApplicationContext so
 * that factory methods can resolve module-owned adapter beans instead of
 * constructing legacy DAO instances.
 */
@Component
public class LegacyDaoFactoryInitializer implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        LegacyDaoFactory.setApplicationContext(applicationContext);
    }
}

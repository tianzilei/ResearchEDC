package org.researchedc.config;

import org.researchedc.dao.spi.DaoProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Initializes {@link DaoProvider} with the Spring {@code ApplicationContext}
 * so that legacy servlets and SOAP endpoints can access Spring-managed DAO
 * beans via the static {@code DaoProvider.getDao()} bridge.
 */
@Component
public class DaoProviderInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        DaoProvider.init(event.getApplicationContext());
    }
}

package org.researchedc.dao.spi;

import org.springframework.context.ApplicationContext;

public final class DaoProvider {

    private static ApplicationContext context;

    private DaoProvider() {
    }

    public static void init(ApplicationContext ctx) {
        context = ctx;
    }

    public static <T> T getDao(Class<T> requiredType) {
        if (context == null) {
            throw new IllegalStateException("DaoProvider not initialized.");
        }
        return context.getBean(requiredType);
    }
}

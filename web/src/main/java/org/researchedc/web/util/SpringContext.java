package org.researchedc.web.util;

import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Static holder for Spring ApplicationContext, set during Spring Boot initialization.
 * Enables legacy servlets (which cannot use field injection) to access Spring beans.
 *
 * <p>Usage: {@code IStudyDAO dao = SpringContext.getBean(IStudyDAO.class);}
 *
 * <p>The context is set by {@code OpenClinicaApplication} during startup.
 */
public final class SpringContext {

    private static ApplicationContext context;

    private SpringContext() {}

    public static void set(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        assertInitialized();
        return (T) context.getBean(name);
    }

    public static <T> T getBean(Class<T> requiredType) {
        assertInitialized();
        return context.getBean(requiredType);
    }

    private static void assertInitialized() {
        if (context == null) {
            throw new IllegalStateException(
                "SpringContext not initialized. " +
                "Ensure OpenClinicaApplication sets SpringContext.context on startup."
            );
        }
    }
}

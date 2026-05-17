package org.akaza.openclinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * OpenClinica Spring Boot Application.
 *
 * <p>Merges the legacy OpenClinica web and ws WAR modules into a single Spring Boot
 * modular monolith. Existing Spring XML configurations are imported via {@code @ImportResource}
 * and will be gradually migrated to Java Config in subsequent iterations.</p>
 *
 * <p>This class also extends {@link OpenClinicaServletInitializer} to support
 * traditional WAR deployment to Tomcat as a fallback.</p>
 */
@SpringBootApplication(scanBasePackages = {
    "org.akaza.openclinica.controller",
    "org.akaza.openclinica.service",
    "org.akaza.openclinica.domain",
    "org.akaza.openclinica.ws",
    "org.akaza.openclinica.web",
    "org.akaza.openclinica.config",
    "org.akaza.openclinica.module"
})
@EntityScan(basePackages = {
    "org.akaza.openclinica.domain",
    "org.akaza.openclinica.module.randomization.entity",
    "org.akaza.openclinica.module.export.entity"
})
@EnableTransactionManagement
@ImportResource(locations = {
    "classpath:org/akaza/openclinica/applicationContext-core-spring.xml",
    "classpath:org/akaza/openclinica/applicationContext-core-db.xml",
    "classpath:org/akaza/openclinica/applicationContext-core-hibernate.xml",
    "classpath:org/akaza/openclinica/applicationContext-core-security.xml",
    "classpath:org/akaza/openclinica/applicationContext-core-service.xml",
    "classpath:org/akaza/openclinica/applicationContext-core-email.xml",
    "classpath:org/akaza/openclinica/applicationContext-core-scheduler.xml",
    "classpath:org/akaza/openclinica/applicationContext-core-annotation-scheduler.xml",
    "classpath:org/akaza/openclinica/applicationContext-core-timer.xml",
    "classpath:org/akaza/openclinica/applicationContext-security.xml",
    "classpath:org/akaza/openclinica/applicationContext-web-beans.xml"
})
@ServletComponentScan(basePackages = {
    "org.akaza.openclinica.control",
    "org.akaza.openclinica.web"
})
public class OpenClinicaApplication extends OpenClinicaServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(OpenClinicaApplication.class, args);
    }
}

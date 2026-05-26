package org.researchedc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.researchedc.service.JobTriggerService;

import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * OpenClinica Spring Boot Application.
 *
 * <p>Merges the legacy OpenClinica web and ws WAR modules into a single Spring Boot
 * modular monolith. All XML configurations have been migrated to Java Config.</p>
 *
 * <p>This class also extends {@link OpenClinicaServletInitializer} to support
 * traditional WAR deployment to Tomcat as a fallback.</p>
 */
@SpringBootApplication
@ComponentScan(
    basePackages = {"org.researchedc"},
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "org\\.researchedc\\.controller\\..*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "org\\.researchedc\\.control\\..*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "org\\.researchedc\\.ws\\..*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "org\\.researchedc\\.job\\..*"
        ),
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            value = JobTriggerService.class
        )
    }
)
@EntityScan(basePackages = {
    "org.researchedc.domain",
    "org.researchedc.module"
})
@EnableTransactionManagement

@ServletComponentScan(basePackages = {
    "org.researchedc.web.filter",
    "org.researchedc.web.listener"
})
public class OpenClinicaApplication extends OpenClinicaServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(OpenClinicaApplication.class, args);
    }
}

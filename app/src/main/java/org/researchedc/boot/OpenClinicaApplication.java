package org.researchedc.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {
    "org.researchedc.config",
    "org.researchedc.module",
    "org.researchedc.service",
    "org.researchedc.web"
})
@EntityScan(basePackages = {
    "org.researchedc.domain",
    "org.researchedc.module"
})
@EnableJpaRepositories(basePackages = {
    "org.researchedc.module"
})
@EnableTransactionManagement
public class OpenClinicaApplication extends OpenClinicaServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(OpenClinicaApplication.class, args);
    }
}

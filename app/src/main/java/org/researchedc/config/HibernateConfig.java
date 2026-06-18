package org.researchedc.config;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Java @Configuration replacing applicationContext-core-hibernate.xml.
 *
 * <p>Legacy DAO beans have been replaced by module-owned primary adapters.
 * This class now only keeps the shared JPA and Hibernate infrastructure that
 * remaining compatibility code still needs.</p>
 */
@Configuration
public class HibernateConfig {

    // ──────────────────────────────────────────────────────────────────────
    //  EntityManagerFactory (for Spring Data JPA repositories)
    // ──────────────────────────────────────────────────────────────────────

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, JpaProperties jpaProperties) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("org.researchedc.domain", "org.researchedc.module");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaPropertyMap(jpaProperties.getProperties());
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SessionFactory (for legacy DAOs that use Hibernate directly)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public SessionFactory sessionFactory(EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Rule DAOs
    // ──────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────
    //  CRF / Item DAOs
    // ──────────────────────────────────────────────────────────────────────

    // SCDItemMetadataDao replaced by @Primary SCDItemMetadataDaoAdapter

    // ──────────────────────────────────────────────────────────────────────
    //  Event / CRF DAOs
    // ──────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────
    //  Study / Subject DAOs
    // ──────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────
    //  User / Auth DAOs
    // ──────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────
    //  Audit / DN DAOs
    // ──────────────────────────────────────────────────────────────────────

    // AuditUserLoginDao replaced by @Primary AuditUserLoginDaoAdapter

    // ──────────────────────────────────────────────────────────────────────
    //  Dynamics / Flag DAOs
    // ──────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────
    //  Misc / Config / Tag DAOs
    // ──────────────────────────────────────────────────────────────────────

    // PasswordRequirements @Bean removed — 0 consumers after admin servlet deletions

    // TagDao and EventDefinitionCrfTagDao deleted - no consumers remain

    // IdtViewDao @Bean removed - zero consumers; impl + SPI deleted

    // DatabaseChangeLogDao legacy SPI deleted; DatabaseChangeLogPort is backed by DatabaseChangeLogDaoAdapter

}

package org.researchedc.config;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.researchedc.dao.QueryStore;
import org.researchedc.dao.hibernate.*;
import org.researchedc.dao.spi.ConfigurationDao;
import org.researchedc.dao.spi.IRuleSetRuleAuditDAO;
import org.researchedc.dao.spi.RuleActionRunLogDomainDao;
import org.researchedc.dao.spi.RuleSetAuditDomainDao;
import org.researchedc.dao.spi.RuleSetDomainDao;
import org.researchedc.dao.spi.SCDItemMetadataDomainDao;
import org.researchedc.dao.spi.IRuleSetRuleDAO;
import org.researchedc.dao.spi.OpenClinicaVersionDao;
import org.researchedc.dao.spi.PasswordRequirements;
import org.researchedc.dao.spi.ResponseSetDomainDao;
import org.researchedc.dao.spi.UsageStatsServiceDao;
import org.researchedc.dao.managestudy.ViewNotesDaoImpl;
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
 * <p>Migrates all 58 legacy Hibernate/DAO beans from XML to Java Config.
 * Infrastructure beans (EntityManagerFactory, TransactionManager, etc.) are
 * auto-configured by Spring Boot and intentionally omitted here.</p>
 *
 * <p>Each AbstractDomainDao subclass gets a no-arg constructor; Spring's
 * {@code @PersistenceContext} on {@link AbstractDomainDao#entityManager}
 * handles EntityManager injection automatically.</p>
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

    // RuleActionRunLogDao replaced by @Primary RuleActionRunLogDaoAdapter

    @Bean
    public RuleSetDomainDao ruleSetDao() {
        return new RuleSetDao();
    }

    // RuleSetAuditDao replaced by @Primary RuleSetAuditDaoAdapter

    // RuleSetRuleDao replaced by @Primary RuleSetRuleDaoAdapter

    // RuleSetRuleDao replaced by @Primary RuleSetRuleDaoAdapter

    // ──────────────────────────────────────────────────────────────────────
    //  CRF / Item DAOs
    // ──────────────────────────────────────────────────────────────────────

    // ItemGroupMetadataDao @Bean removed - no consumers inject concrete type;
    // IItemGroupMetadataDAO consumers go through @Primary ItemGroupMetadataDaoAdapter

    // SCDItemMetadataDao replaced by @Primary SCDItemMetadataDaoAdapter

    // ──────────────────────────────────────────────────────────────────────
    //  Event / CRF DAOs
    // ──────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────
    //  Study / Subject DAOs
    // ──────────────────────────────────────────────────────────────────────

    // StudyParameterValueDao replaced by @Primary StudyParameterValueDaoAdapter

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

    // DynamicsItemFormMetadataDao replaced by @Primary DynamicsItemFormMetadataDaoAdapter

    // DynamicsItemGroupMetadataDao replaced by @Primary DynamicsItemGroupMetadataDaoAdapter

    // ──────────────────────────────────────────────────────────────────────
    //  Misc / Config / Tag DAOs
    // ──────────────────────────────────────────────────────────────────────

    // ConfigurationDao replaced by @Primary ConfigurationDaoAdapter

    @Bean
    public PasswordRequirements passwordRequirements(org.researchedc.dao.spi.ConfigurationDao configurationDao) {
        return new PasswordRequirementsDao(configurationDao);
    }

    // TagDao deleted - no consumers; EventDefinitionCrfTagDaoAdapter serves tagging

    // IdtViewDao @Bean removed - zero consumers; impl + SPI deleted

    @Bean
    public UsageStatsServiceDao usageStatsServiceDAO() {
        return new UsageStatsServiceDAO();
    }

    @Bean
    public OpenClinicaVersionDao openClinicaVersionDAO() {
        return new OpenClinicaVersionDAO();
    }

    // DatabaseChangeLogDao replaced by @Primary DatabaseChangeLogDaoAdapter

    // ──────────────────────────────────────────────────────────────────────
    //  Special: viewNotesDao (uses dataSource + queryStore, not JPA)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public ViewNotesDaoImpl viewNotesDao(DataSource dataSource, QueryStore queryStore) {
        ViewNotesDaoImpl dao = new ViewNotesDaoImpl();
        dao.setDataSource(dataSource);
        dao.setQueryStore(queryStore);
        return dao;
    }
}

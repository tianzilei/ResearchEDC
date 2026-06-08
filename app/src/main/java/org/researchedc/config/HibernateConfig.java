package org.researchedc.config;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.researchedc.dao.QueryStore;
import org.researchedc.dao.hibernate.*;
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

    @Bean
    public RuleActionRunLogDomainDao ruleActionRunLogDao() {
        return new RuleActionRunLogDao();
    }

    @Bean
    public RuleSetDomainDao ruleSetDao() {
        return new RuleSetDao();
    }

    @Bean
    public RuleSetAuditDomainDao ruleSetAuditDao() {
        return new RuleSetAuditDao();
    }

    @Bean
    public IRuleSetRuleDAO ruleSetRuleDao() {
        return new RuleSetRuleDao();
    }

    @Bean
    public IRuleSetRuleAuditDAO ruleSetRuleAuditDao() {
        return new RuleSetRuleAuditDao();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  CRF / Item DAOs
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public ItemGroupMetadataDao itemGroupMetadataDao() {
        return new ItemGroupMetadataDao();
    }

    @Bean
    public SCDItemMetadataDomainDao scdItemMetadataDao() {
        return new SCDItemMetadataDao();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Event / CRF DAOs
    // ──────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────
    //  Study / Subject DAOs
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public StudyParameterValueDao studyParameterValueDao() {
        return new StudyParameterValueDao();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  User / Auth DAOs
    // ──────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────
    //  Audit / DN DAOs
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public AuditLogEventDao auditLogEventDaoDomain() {
        return new AuditLogEventDao();
    }

    @Bean
    public AuditUserLoginDao auditUserLoginDao() {
        return new AuditUserLoginDao();
    }

    @Bean
    public DiscrepancyNoteDao discrepancyNoteDao() {
        return new DiscrepancyNoteDao();
    }

    @Bean
    public DiscrepancyNoteTypeDao discrepancyNoteTypeDao() {
        return new DiscrepancyNoteTypeDao();
    }

    @Bean
    public ResolutionStatusDao resolutionStatusDao() {
        return new ResolutionStatusDao();
    }

    @Bean
    public DnItemDataMapDao dnItemDataMapDao() {
        return new DnItemDataMapDao();
    }

    @Bean
    public CompletionStatusDao completionStatusDao() {
        return new CompletionStatusDao();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Dynamics / Flag DAOs
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao() {
        return new DynamicsItemFormMetadataDao();
    }

    @Bean
    public DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao() {
        return new DynamicsItemGroupMetadataDao();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Misc / Config / Tag DAOs
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public ConfigurationDao configurationDao() {
        return new ConfigurationDao();
    }

    @Bean
    public PasswordRequirements passwordRequirements(ConfigurationDao configurationDao) {
        return new PasswordRequirementsDao(configurationDao);
    }

    @Bean
    public TagDao tagDao() {
        return new TagDao();
    }

    @Bean
    public IdtViewDao idtViewDao() {
        return new IdtViewDao();
    }

    @Bean
    public UsageStatsServiceDao usageStatsServiceDAO() {
        return new UsageStatsServiceDAO();
    }

    @Bean
    public OpenClinicaVersionDao openClinicaVersionDAO() {
        return new OpenClinicaVersionDAO();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Special: databaseChangeLogDao (uses SessionFactory, not AbstractDomainDao)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public DatabaseChangeLogDao databaseChangeLogDao(SessionFactory sessionFactory) {
        DatabaseChangeLogDao dao = new DatabaseChangeLogDao();
        dao.setSessionFactory(sessionFactory);
        return dao;
    }

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

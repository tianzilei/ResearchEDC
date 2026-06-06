package org.researchedc.config;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.researchedc.dao.QueryStore;
import org.researchedc.dao.hibernate.*;
import org.researchedc.dao.spi.IRuleSetRuleAuditDAO;
import org.researchedc.dao.spi.RuleActionPropertyDomainDao;
import org.researchedc.dao.spi.RuleActionRunLogDomainDao;
import org.researchedc.dao.spi.RuleDomainDao;
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
    public RuleDomainDao ruleDao() {
        return new RuleDao();
    }

    @Bean
    public RuleActionPropertyDomainDao ruleActionPropertyDao() {
        return new RuleActionPropertyDao();
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
    public CrfDao crfDao() {
        return new CrfDao();
    }

    @Bean
    public CrfVersionDao crfVersionDao() {
        return new CrfVersionDao();
    }

    @Bean
    public CrfVersionMediaDao crfVersionMediaDao() {
        return new CrfVersionMediaDao();
    }

    @Bean
    public SectionDao sectionDao() {
        return new SectionDao();
    }

    @Bean
    public ItemGroupDao itemGroupDao() {
        return new ItemGroupDao();
    }

    @Bean
    public ItemDao itemDao() {
        return new ItemDao();
    }

    @Bean
    public ItemDataDao itemDataDao() {
        return new ItemDataDao();
    }

    @Bean
    public ItemReferenceTypeDao itemReferenceTypeDao() {
        return new ItemReferenceTypeDao();
    }

    @Bean
    public ItemDataTypeDao itemDataTypeDao() {
        return new ItemDataTypeDao();
    }

    @Bean
    public ItemFormMetadataDao itemFormMetadataDao() {
        return new ItemFormMetadataDao();
    }

    @Bean
    public ItemGroupMetadataDao itemGroupMetadataDao() {
        return new ItemGroupMetadataDao();
    }

    @Bean
    public ResponseSetDomainDao responseSetDao() {
        return new ResponseSetDao();
    }

    @Bean
    public ResponseTypeDao responseTypeDao() {
        return new ResponseTypeDao();
    }

    @Bean
    public VersioningMapDao versioningMapDao() {
        return new VersioningMapDao();
    }

    @Bean
    public SCDItemMetadataDomainDao scdItemMetadataDao() {
        return new SCDItemMetadataDao();
    }

    @Bean
    public MeasurementUnitDao measurementUnitDao() {
        return new MeasurementUnitDao();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Event / CRF DAOs
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public EventCrfDao eventCrfDao() {
        return new EventCrfDao();
    }

    @Bean
    public EventDefinitionCrfDao eventDefinitionCrfDao() {
        return new EventDefinitionCrfDao();
    }

    @Bean
    public EventCrfFlagDao eventCrfFlagDao() {
        return new EventCrfFlagDao();
    }

    @Bean
    public EventCrfFlagWorkflowDao eventCrfFlagWorkflowDao() {
        return new EventCrfFlagWorkflowDao();
    }

    @Bean
    public ItemDataFlagDao itemDataFlagDao() {
        return new ItemDataFlagDao();
    }

    @Bean
    public ItemDataFlagWorkflowDao itemDataFlagWorkflowDao() {
        return new ItemDataFlagWorkflowDao();
    }

    @Bean
    public EventDefinitionCrfItemTagDao eventDefinitionCrfItemTagDao() {
        return new EventDefinitionCrfItemTagDao();
    }

    @Bean
    public EventDefinitionCrfTagDao eventDefinitionCrfTagDao() {
        return new EventDefinitionCrfTagDao();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Study / Subject DAOs
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public StudyDao studyDaoDomain() {
        return new StudyDao();
    }

    @Bean
    public StudySubjectDao studySubjectDaoDomain() {
        return new StudySubjectDao();
    }

    @Bean
    public StudyEventDefinitionDao studyEventDefDaoDomain() {
        return new StudyEventDefinitionDao();
    }

    @Bean
    public StudyEventDao studyEventDaoDomain() {
        return new StudyEventDao();
    }

    @Bean
    public StudyUserRoleDao studyUserRoleDao() {
        return new StudyUserRoleDao();
    }

    @Bean
    public StudyParameterValueDao studyParameterValueDao() {
        return new StudyParameterValueDao();
    }

    @Bean
    public StudyModuleStatusDao studyModuleStatusDao() {
        return new StudyModuleStatusDao();
    }

    @Bean
    public SubjectDao subjectDao() {
        return new SubjectDao();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  User / Auth DAOs
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public UserAccountDao userDaoDomain() {
        return new UserAccountDao();
    }

    @Bean
    public UserTypeDao userTypeDao() {
        return new UserTypeDao();
    }

    @Bean
    public AuthoritiesDao authoritiesDao() {
        return new AuthoritiesDao();
    }

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

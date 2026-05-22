package org.researchedc.config;

import javax.sql.DataSource;

import org.researchedc.core.CRFLocker;
import org.researchedc.dao.extract.ArchivedDatasetFileDAO;
import org.researchedc.dao.extract.DatasetDAO;
import org.researchedc.dao.spi.AuditDao;
import org.researchedc.dao.spi.IAuditEventDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IItemDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.researchedc.dao.spi.IItemGroupDAO;
import org.researchedc.dao.spi.IItemGroupMetadataDAO;
import org.researchedc.dao.spi.ISectionDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.hibernate.AuditLogEventDao;
import org.researchedc.dao.hibernate.EventDefinitionCrfTagDao;
import org.researchedc.legacy.dao.AuditDaoImpl;
import org.researchedc.legacy.dao.IAuditEventDAOImpl;
import org.researchedc.legacy.dao.CRFVersionDaoImpl;
import org.researchedc.legacy.dao.ICrfDAOImpl;
import org.researchedc.legacy.dao.IItemDAOImpl;
import org.researchedc.legacy.dao.IItemDataDAOImpl;
import org.researchedc.legacy.dao.IItemFormMetadataDAOImpl;
import org.researchedc.legacy.dao.IItemGroupDAOImpl;
import org.researchedc.legacy.dao.IItemGroupMetadataDAOImpl;
import org.researchedc.legacy.dao.ISectionDAOImpl;
import org.researchedc.legacy.dao.ISubjectDAOImpl;
import org.researchedc.legacy.dao.IUserAccountDAOImpl;
import org.researchedc.dao.hibernate.RuleActionRunLogDao;
import org.researchedc.dao.hibernate.RuleSetDao;
import org.researchedc.dao.hibernate.RuleSetRuleDao;
import org.researchedc.dao.hibernate.StudyDao;
import org.researchedc.dao.hibernate.StudyEventDao;
import org.researchedc.dao.hibernate.StudyEventDefinitionDao;
import org.researchedc.dao.hibernate.StudySubjectDao;
import org.researchedc.dao.hibernate.StudyUserRoleDao;
import org.researchedc.dao.hibernate.UserAccountDao;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.managestudy.ViewNotesDao;
import org.researchedc.dao.core.CoreResources;
import org.researchedc.service.EventService;
import org.researchedc.service.RandomizeService;
import org.researchedc.service.crfdata.BeanPropertyService;
import org.researchedc.service.crfdata.DynamicsMetadataService;
import org.researchedc.service.crfdata.InstantOnChangeService;
import org.researchedc.service.crfdata.SimpleConditionalDisplayService;
import org.researchedc.service.extract.GenerateClinicalDataServiceImpl;
import org.researchedc.service.extract.GenerateExtractFileService;
import org.researchedc.service.extract.OdmFileCreation;
import org.researchedc.service.managestudy.EventDefinitionCrfTagService;
import org.researchedc.service.managestudy.StudySubjectServiceImpl;
import org.researchedc.service.managestudy.ViewNotesServiceImpl;
import org.researchedc.service.rule.RuleSetListenerService;
import org.researchedc.service.rule.RuleSetService;
import org.researchedc.service.rule.RulesPostImportContainerService;
import org.researchedc.service.rule.StudyEventBeanListener;
import org.researchedc.service.rule.expression.ExpressionService;
import org.researchedc.service.subject.SubjectService;
import org.researchedc.validator.rule.action.EventActionValidator;
import org.researchedc.validator.rule.action.InsertActionValidator;
import org.researchedc.validator.rule.action.RandomizeActionValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Java @Configuration replacing applicationContext-core-service.xml.
 *
 * <p>Migrates all 27 legacy service/DAO beans from XML to Java Config.
 * Beans that were defined in applicationContext-core-hibernate.xml (e.g. studyEventDaoDomain,
 * userDaoDomain, ruleSetDao, etc.) are injected as method parameters — Spring resolves them
 * from the remaining XML config or future Java Config.</p>
 */
@Configuration
public class ServiceConfig {

    private final DataSource dataSource;

    public ServiceConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Simple DAO beans (constructor: DataSource only)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public IAuditEventDAO auditEventDao() {
        return new IAuditEventDAOImpl(dataSource);
    }

    @Bean
    public AuditDao auditJdbcDao() {
        return new AuditDaoImpl(dataSource);
    }

    @Bean
    public ICrfDAO crfDao() {
        return new ICrfDAOImpl(dataSource);
    }

    @Bean
    public DatasetDAO datasetDao() {
        return new DatasetDAO(dataSource);
    }

    @Bean
    public StudyEventDAO studyeventdaojdbc() {
        return new StudyEventDAO(dataSource);
    }

    @Bean
    public IUserAccountDAO userAccountDao() {
        return new IUserAccountDAOImpl(dataSource);
    }

    @Bean
    public ArchivedDatasetFileDAO archivedDatasetFileDao() {
        return new ArchivedDatasetFileDAO(dataSource);
    }

    @Bean
    public IItemDataDAO itemDataDao() {
        return new IItemDataDAOImpl(dataSource);
    }

    @Bean
    public IItemDAO itemDao() {
        return new IItemDAOImpl(dataSource);
    }

    @Bean
    public ICrfVersionDAO crfVersionJdbcDao() {
        return new CRFVersionDaoImpl(dataSource);
    }

    @Bean
    public ISectionDAO sectionJdbcDao() {
        return new ISectionDAOImpl(dataSource);
    }

    @Bean
    public IItemFormMetadataDAO itemFormMetadataJdbcDao() {
        return new IItemFormMetadataDAOImpl(dataSource);
    }

    @Bean
    public IItemGroupDAO itemGroupJdbcDao() {
        return new IItemGroupDAOImpl(dataSource);
    }

    @Bean
    public IItemGroupMetadataDAO itemGroupMetadataJdbcDao() {
        return new IItemGroupMetadataDAOImpl(dataSource);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Simple service beans (constructor: DataSource only)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public RandomizeService randomizeService() {
        return new RandomizeService(dataSource);
    }

    @Bean
    public ISubjectDAO subjectJdbcDao() {
        return new ISubjectDAOImpl(dataSource);
    }

    @Bean
    public SubjectService subjectService() {
        return new SubjectService(dataSource);
    }

    @Bean
    public EventService eventService() {
        return new EventService(dataSource);
    }

    @Bean
    public InsertActionValidator insertActionValidator() {
        return new InsertActionValidator(dataSource);
    }

    @Bean
    public EventActionValidator eventActionValidator() {
        return new EventActionValidator(dataSource);
    }

    @Bean
    public RandomizeActionValidator randomizeActionValidator() {
        return new RandomizeActionValidator(dataSource);
    }

    @Bean
    public SimpleConditionalDisplayService simpleConditionalDisplayService() {
        return new SimpleConditionalDisplayService(dataSource);
    }

    @Bean
    public InstantOnChangeService instantOnChangeService() {
        return new InstantOnChangeService(dataSource);
    }

    @Bean
    public ExpressionService ExpressionService() {
        return new ExpressionService(dataSource);
    }

    @Bean
    public RulesPostImportContainerService rulesPostImportContainerService() {
        return new RulesPostImportContainerService(dataSource);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  CRFLocker (no-arg constructor)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public CRFLocker crfLocker() {
        return new CRFLocker();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Beans with constructor + property injection
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public DynamicsMetadataService dynamicsMetadataService(RandomizeService randomizeService) {
        DynamicsMetadataService s = new DynamicsMetadataService(dataSource);
        s.setRandomizeService(randomizeService);
        return s;
    }

    @Bean
    public BeanPropertyService beanPropertyService(
            StudyEventDao studyEventDaoDomain,
            StudyEventDefinitionDao studyEventDefDaoDomain,
            StudySubjectDao studySubjectDaoDomain,
            UserAccountDao userDaoDomain) {
        BeanPropertyService s = new BeanPropertyService(dataSource);
        s.setStudyEventDAO(studyEventDaoDomain);
        s.setStudyEventDefinitionDao(studyEventDefDaoDomain);
        s.setStudySubjectDao(studySubjectDaoDomain);
        s.setUserAccountDao(userDaoDomain);
        return s;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  StudyEventBeanListener (constructor: StudyEventDAO + setter: dataSource)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public StudyEventBeanListener studyEventBeanListener(StudyEventDAO studyeventdaojdbc) {
        StudyEventBeanListener s = new StudyEventBeanListener(studyeventdaojdbc);
        s.setDataSource(dataSource);
        return s;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  GenerateExtractFileService (3-arg constructor)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public GenerateExtractFileService generateExtractFileService(
            CoreResources coreResources,
            RuleSetRuleDao ruleSetRuleDao) {
        return new GenerateExtractFileService(dataSource, coreResources, ruleSetRuleDao);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Beans with property injection only (no constructor args)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public RuleSetService ruleSetService(
            BeanPropertyService beanPropertyService,
            StudyEventDao studyEventDaoDomain,
            StudyEventDefinitionDao studyEventDefDaoDomain,
            RuleActionRunLogDao ruleActionRunLogDao) {
        RuleSetService s = new RuleSetService();
        s.setDataSource(dataSource);
        s.setBeanPropertyService(beanPropertyService);
        s.setStudyEventDomainDao(studyEventDaoDomain);
        s.setStudyEventDefDomainDao(studyEventDefDaoDomain);
        s.setRuleActionRunLogDao(ruleActionRunLogDao);
        return s;
    }

    @Bean
    public RuleSetListenerService ruleSetListenerService(
            RuleSetService ruleSetService,
            RuleSetDao ruleSetDao) {
        RuleSetListenerService s = new RuleSetListenerService();
        s.setRuleSetService(ruleSetService);
        s.setRuleSetDao(ruleSetDao);
        return s;
    }

    @Bean
    public OdmFileCreation odmFileCreation(
            CoreResources coreResources,
            RuleSetRuleDao ruleSetRuleDao) {
        OdmFileCreation s = new OdmFileCreation();
        s.setDataSource(dataSource);
        s.setCoreResources(coreResources);
        s.setRuleSetRuleDao(ruleSetRuleDao);
        return s;
    }

    @Bean
    public StudySubjectServiceImpl studySubjectService() {
        StudySubjectServiceImpl s = new StudySubjectServiceImpl();
        s.setDataSource(dataSource);
        return s;
    }

    @Bean
    public EventDefinitionCrfTagService eventDefinitionCrfTagService(
            EventDefinitionCrfTagDao eventDefinitionCrfTagDao,
            UserAccountDao userDaoDomain) {
        EventDefinitionCrfTagService s = new EventDefinitionCrfTagService();
        s.setEventDefinitionCrfTagDao(eventDefinitionCrfTagDao);
        s.setUserDaoDomain(userDaoDomain);
        return s;
    }

    @Bean
    public ViewNotesServiceImpl viewNotesService(ViewNotesDao viewNotesDao) {
        ViewNotesServiceImpl s = new ViewNotesServiceImpl();
        s.setViewNotesDao(viewNotesDao);
        return s;
    }

    @Bean
    public GenerateClinicalDataServiceImpl generateClinicalDataService(
            StudyDao studyDaoDomain,
            StudySubjectDao studySubjectDaoDomain,
            StudyEventDefinitionDao studyEventDefDaoDomain,
            AuditLogEventDao auditLogEventDaoDomain,
            UserAccountDao userDaoDomain,
            StudyUserRoleDao studyUserRoleDao) {
        GenerateClinicalDataServiceImpl s = new GenerateClinicalDataServiceImpl();
        s.setStudyDao(studyDaoDomain);
        s.setStudySubjectDao(studySubjectDaoDomain);
        s.setStudyEventDefDao(studyEventDefDaoDomain);
        s.setAuditEventDAO(auditLogEventDaoDomain);
        s.setUserAccountDao(userDaoDomain);
        s.setStudyUserRoleDao(studyUserRoleDao);
        return s;
    }
}

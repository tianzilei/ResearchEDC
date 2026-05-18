package org.akaza.openclinica.config;

import javax.sql.DataSource;

import org.akaza.openclinica.core.CRFLocker;
import org.akaza.openclinica.dao.admin.AuditEventDAO;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfTagDao;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.ViewNotesDao;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.service.EventService;
import org.akaza.openclinica.service.RandomizeService;
import org.akaza.openclinica.service.crfdata.BeanPropertyService;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.akaza.openclinica.service.crfdata.InstantOnChangeService;
import org.akaza.openclinica.service.crfdata.SimpleConditionalDisplayService;
import org.akaza.openclinica.service.extract.GenerateClinicalDataServiceImpl;
import org.akaza.openclinica.service.extract.GenerateExtractFileService;
import org.akaza.openclinica.service.extract.OdmFileCreation;
import org.akaza.openclinica.service.managestudy.EventDefinitionCrfTagService;
import org.akaza.openclinica.service.managestudy.StudySubjectServiceImpl;
import org.akaza.openclinica.service.managestudy.ViewNotesServiceImpl;
import org.akaza.openclinica.service.rule.RuleSetListenerService;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.service.rule.RulesPostImportContainerService;
import org.akaza.openclinica.service.rule.StudyEventBeanListener;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.akaza.openclinica.service.subject.SubjectService;
import org.akaza.openclinica.validator.rule.action.EventActionValidator;
import org.akaza.openclinica.validator.rule.action.InsertActionValidator;
import org.akaza.openclinica.validator.rule.action.RandomizeActionValidator;
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
    public AuditEventDAO auditEventDao() {
        return new AuditEventDAO(dataSource);
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
    public UserAccountDAO userAccountDao() {
        return new UserAccountDAO(dataSource);
    }

    @Bean
    public ArchivedDatasetFileDAO archivedDatasetFileDao() {
        return new ArchivedDatasetFileDAO(dataSource);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Simple service beans (constructor: DataSource only)
    // ──────────────────────────────────────────────────────────────────────

    @Bean
    public RandomizeService randomizeService() {
        return new RandomizeService(dataSource);
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

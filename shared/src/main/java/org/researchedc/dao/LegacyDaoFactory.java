package org.researchedc.dao;

import javax.sql.DataSource;

import org.researchedc.dao.spi.ArchivedDatasetFileDao;
import org.researchedc.dao.spi.DatasetDao;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.EventDefinitionCRFDao;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.spi.IItemDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IItemGroupDAO;
import org.researchedc.dao.spi.IItemGroupMetadataDAO;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.researchedc.dao.spi.ISectionDAO;
import org.researchedc.dao.spi.IRuleDAO;
import org.researchedc.dao.spi.IRuleSetDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.dao.spi.StudyGroupDao;
import org.springframework.context.ApplicationContext;

public final class LegacyDaoFactory {

    private static volatile ApplicationContext applicationContext;

    private LegacyDaoFactory() {
    }

    public static void setApplicationContext(ApplicationContext ctx) {
        applicationContext = ctx;
    }

    public static IStudyDAO studyDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IStudyDAO.class)
                : null;
    }

    public static IStudySubjectDAO studySubjectDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IStudySubjectDAO.class)
                : null;
    }

    public static IStudyEventDefinitionDAO studyEventDefinitionDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IStudyEventDefinitionDAO.class)
                : null;
    }

    public static IStudyEventDAO studyEventDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IStudyEventDAO.class)
                : null;
    }

    public static EventDefinitionCRFDao eventDefinitionCrfDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(EventDefinitionCRFDao.class)
                : null;
    }

    public static ISubjectDAO subjectDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(ISubjectDAO.class)
                : null;
    }

    public static IUserAccountDAO userAccountDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IUserAccountDAO.class)
                : null;
    }

    public static ICrfDAO crfDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(ICrfDAO.class)
                : null;
    }

    public static ICrfVersionDAO crfVersionDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(ICrfVersionDAO.class)
                : null;
    }

    public static IDiscrepancyNoteDAO discrepancyNoteDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IDiscrepancyNoteDAO.class)
                : null;
    }

    public static EventCRFDao eventCrfDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(EventCRFDao.class)
                : null;
    }

    public static IItemDataDAO itemDataDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IItemDataDAO.class)
                : null;
    }

    public static IItemDAO itemDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IItemDAO.class)
                : null;
    }

    public static IItemGroupDAO itemGroupDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IItemGroupDAO.class)
                : null;
    }

    public static IItemFormMetadataDAO itemFormMetadataDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IItemFormMetadataDAO.class)
                : null;
    }

    public static ISectionDAO sectionDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(ISectionDAO.class)
                : null;
    }

    public static ArchivedDatasetFileDao archivedDatasetFileDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(ArchivedDatasetFileDao.class)
                : null;
    }

    public static IItemGroupMetadataDAO itemGroupMetadataDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IItemGroupMetadataDAO.class)
                : null;
    }

    public static IRuleDAO ruleDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IRuleDAO.class)
                : null;
    }

    public static IRuleSetDAO ruleSetDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(IRuleSetDAO.class)
                : null;
    }

    public static DatasetDao datasetDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(DatasetDao.class)
                : null;
    }

    public static StudyGroupClassDao studyGroupClassDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(StudyGroupClassDao.class)
                : null;
    }

    public static StudyGroupDao studyGroupDao(DataSource dataSource) {
        return applicationContext != null
                ? applicationContext.getBean(StudyGroupDao.class)
                : null;
    }
}

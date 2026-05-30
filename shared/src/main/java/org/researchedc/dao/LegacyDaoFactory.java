package org.researchedc.dao;

import javax.sql.DataSource;

import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.managestudy.StudySubjectDAO;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.dao.spi.EventCRFDao;
import org.researchedc.dao.spi.IItemDAO;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.dao.spi.IItemGroupDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.dao.submit.ItemGroupDAO;
import org.researchedc.dao.submit.SubjectDAO;

public final class LegacyDaoFactory {

    private LegacyDaoFactory() {
    }

    public static IStudyDAO studyDao(DataSource dataSource) {
        return new StudyDAO(dataSource);
    }

    public static IStudySubjectDAO studySubjectDao(DataSource dataSource) {
        return new StudySubjectDAO(dataSource);
    }

    public static ISubjectDAO subjectDao(DataSource dataSource) {
        return new SubjectDAO(dataSource);
    }

    public static IUserAccountDAO userAccountDao(DataSource dataSource) {
        return new UserAccountDAO(dataSource);
    }

    public static ICrfDAO crfDao(DataSource dataSource) {
        return new CRFDAO(dataSource);
    }

    public static ICrfVersionDAO crfVersionDao(DataSource dataSource) {
        return new CRFVersionDAO(dataSource);
    }

    public static IDiscrepancyNoteDAO discrepancyNoteDao(DataSource dataSource) {
        return new DiscrepancyNoteDAO(dataSource);
    }

    public static EventCRFDao eventCrfDao(DataSource dataSource) {
        return new EventCRFDAO(dataSource);
    }

    public static IItemDataDAO itemDataDao(DataSource dataSource) {
        return new ItemDataDAO(dataSource);
    }

    public static IItemDAO itemDao(DataSource dataSource) {
        return new ItemDAO(dataSource);
    }

    public static IItemGroupDAO itemGroupDao(DataSource dataSource) {
        return new ItemGroupDAO(dataSource);
    }
}

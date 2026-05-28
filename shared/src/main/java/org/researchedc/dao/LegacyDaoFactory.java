package org.researchedc.dao;

import javax.sql.DataSource;

import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.managestudy.StudySubjectDAO;
import org.researchedc.dao.spi.IStudyDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;
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
}

package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.managestudy.StudySubjectDAO;
import org.researchedc.dao.spi.IStudySubjectDAO;

public class IStudySubjectDAOImpl extends StudySubjectDAO implements IStudySubjectDAO {

    public IStudySubjectDAOImpl(DataSource ds) {
        super(ds);
    }
}

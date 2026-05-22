package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.managestudy.StudyGroupClassDAO;
import org.researchedc.dao.spi.StudyGroupClassDao;

public class StudyGroupClassDaoImpl extends StudyGroupClassDAO implements StudyGroupClassDao {

    public StudyGroupClassDaoImpl(DataSource ds) {
        super(ds);
    }
}

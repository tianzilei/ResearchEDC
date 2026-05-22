package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.managestudy.StudyGroupDAO;
import org.researchedc.dao.spi.StudyGroupDao;

public class StudyGroupDaoImpl extends StudyGroupDAO implements StudyGroupDao {

    public StudyGroupDaoImpl(DataSource ds) {
        super(ds);
    }
}

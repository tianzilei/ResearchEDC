package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.spi.IStudyDAO;

public class IStudyDAOImpl extends StudyDAO implements IStudyDAO {

    public IStudyDAOImpl(DataSource ds) {
        super(ds);
    }
}

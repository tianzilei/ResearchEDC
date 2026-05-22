package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.managestudy.StudyEventDAO;
import org.researchedc.dao.spi.IStudyEventDAO;

public class IStudyEventDAOImpl extends StudyEventDAO implements IStudyEventDAO {

    public IStudyEventDAOImpl(DataSource ds) {
        super(ds);
    }
}

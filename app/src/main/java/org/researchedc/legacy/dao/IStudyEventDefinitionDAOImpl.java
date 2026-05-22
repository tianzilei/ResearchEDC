package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.managestudy.StudyEventDefinitionDAO;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;

public class IStudyEventDefinitionDAOImpl extends StudyEventDefinitionDAO implements IStudyEventDefinitionDAO {

    public IStudyEventDefinitionDAOImpl(DataSource ds) {
        super(ds);
    }
}

package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.service.StudyParameterValueDAO;
import org.researchedc.dao.spi.IStudyParameterValueDAO;

public class IStudyParameterValueDAOImpl extends StudyParameterValueDAO implements IStudyParameterValueDAO {

    public IStudyParameterValueDAOImpl(DataSource ds) {
        super(ds);
    }
}

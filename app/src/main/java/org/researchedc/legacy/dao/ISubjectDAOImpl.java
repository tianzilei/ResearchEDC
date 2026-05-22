package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.submit.SubjectDAO;
import org.researchedc.dao.spi.ISubjectDAO;

public class ISubjectDAOImpl extends SubjectDAO implements ISubjectDAO {

    public ISubjectDAOImpl(DataSource ds) {
        super(ds);
    }
}

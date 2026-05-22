package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.submit.SectionDAO;
import org.researchedc.dao.spi.ISectionDAO;

public class ISectionDAOImpl extends SectionDAO implements ISectionDAO {

    public ISectionDAOImpl(DataSource ds) {
        super(ds);
    }
}

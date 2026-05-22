package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.dao.spi.ICrfVersionDAO;

public class CRFVersionDaoImpl extends CRFVersionDAO implements ICrfVersionDAO {

    public CRFVersionDaoImpl(DataSource ds) {
        super(ds);
    }
}

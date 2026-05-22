package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.spi.ICrfDAO;

public class ICrfDAOImpl extends CRFDAO implements ICrfDAO {

    public ICrfDAOImpl(DataSource ds) {
        super(ds);
    }
}

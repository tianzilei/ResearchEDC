package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.submit.ItemDAO;
import org.researchedc.dao.spi.IItemDAO;

public class IItemDAOImpl extends ItemDAO implements IItemDAO {

    public IItemDAOImpl(DataSource ds) {
        super(ds);
    }
}

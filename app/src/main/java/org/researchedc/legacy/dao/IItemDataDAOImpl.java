package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.submit.ItemDataDAO;
import org.researchedc.dao.spi.IItemDataDAO;

public class IItemDataDAOImpl extends ItemDataDAO implements IItemDataDAO {

    public IItemDataDAOImpl(DataSource ds) {
        super(ds);
    }
}

package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.submit.ItemGroupDAO;
import org.researchedc.dao.spi.IItemGroupDAO;

public class IItemGroupDAOImpl extends ItemGroupDAO implements IItemGroupDAO {

    public IItemGroupDAOImpl(DataSource ds) {
        super(ds);
    }
}

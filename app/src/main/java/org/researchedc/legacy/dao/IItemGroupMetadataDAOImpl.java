package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.submit.ItemGroupMetadataDAO;
import org.researchedc.dao.spi.IItemGroupMetadataDAO;

public class IItemGroupMetadataDAOImpl extends ItemGroupMetadataDAO implements IItemGroupMetadataDAO {

    public IItemGroupMetadataDAOImpl(DataSource ds) {
        super(ds);
    }
}

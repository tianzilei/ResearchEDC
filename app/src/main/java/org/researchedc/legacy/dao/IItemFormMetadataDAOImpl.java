package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.dao.spi.IItemFormMetadataDAO;

public class IItemFormMetadataDAOImpl extends ItemFormMetadataDAO implements IItemFormMetadataDAO {

    public IItemFormMetadataDAOImpl(DataSource ds) {
        super(ds);
    }
}

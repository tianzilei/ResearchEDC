package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.extract.DatasetDAO;
import org.researchedc.dao.spi.DatasetDao;

public class DatasetDaoImpl extends DatasetDAO implements DatasetDao {

    public DatasetDaoImpl(DataSource ds) {
        super(ds);
    }
}

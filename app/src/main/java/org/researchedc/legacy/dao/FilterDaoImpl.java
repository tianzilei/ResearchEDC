package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.extract.FilterDAO;
import org.researchedc.dao.spi.FilterDao;

public class FilterDaoImpl extends FilterDAO implements FilterDao {

    public FilterDaoImpl(DataSource ds) {
        super(ds);
    }
}

package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.extract.ArchivedDatasetFileDAO;
import org.researchedc.dao.spi.ArchivedDatasetFileDao;

public class ArchivedDatasetFileDaoImpl extends ArchivedDatasetFileDAO implements ArchivedDatasetFileDao {

    public ArchivedDatasetFileDaoImpl(DataSource ds) {
        super(ds);
    }
}

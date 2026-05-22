package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.submit.SubjectGroupMapDAO;
import org.researchedc.dao.spi.SubjectGroupMapDao;

public class SubjectGroupMapDaoImpl extends SubjectGroupMapDAO implements SubjectGroupMapDao {

    public SubjectGroupMapDaoImpl(DataSource ds) {
        super(ds);
    }
}

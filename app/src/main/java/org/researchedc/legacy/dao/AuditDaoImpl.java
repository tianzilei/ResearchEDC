package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.admin.AuditDAO;
import org.researchedc.dao.spi.AuditDao;

public class AuditDaoImpl extends AuditDAO implements AuditDao {

    public AuditDaoImpl(DataSource ds) {
        super(ds);
    }
}

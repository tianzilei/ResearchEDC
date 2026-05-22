package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.admin.AuditEventDAO;
import org.researchedc.dao.spi.IAuditEventDAO;

public class IAuditEventDAOImpl extends AuditEventDAO implements IAuditEventDAO {

    public IAuditEventDAOImpl(DataSource ds) {
        super(ds);
    }
}

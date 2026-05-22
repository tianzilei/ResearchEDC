package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.spi.IUserAccountDAO;

public class IUserAccountDAOImpl extends UserAccountDAO implements IUserAccountDAO {

    public IUserAccountDAOImpl(DataSource ds) {
        super(ds);
    }
}

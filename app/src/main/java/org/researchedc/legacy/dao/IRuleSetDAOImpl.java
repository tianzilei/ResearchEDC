package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.rule.RuleSetDAO;
import org.researchedc.dao.spi.IRuleSetDAO;

public class IRuleSetDAOImpl extends RuleSetDAO implements IRuleSetDAO {

    public IRuleSetDAOImpl(DataSource ds) {
        super(ds);
    }
}

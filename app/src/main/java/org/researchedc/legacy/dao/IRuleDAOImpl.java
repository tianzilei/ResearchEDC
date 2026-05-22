package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.rule.RuleDAO;
import org.researchedc.dao.spi.IRuleDAO;

public class IRuleDAOImpl extends RuleDAO implements IRuleDAO {

    public IRuleDAOImpl(DataSource ds) {
        super(ds);
    }
}

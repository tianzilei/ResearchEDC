package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.managestudy.EventDefinitionCRFDAO;
import org.researchedc.dao.spi.EventDefinitionCRFDao;

public class EventDefinitionCRFDaoImpl extends EventDefinitionCRFDAO implements EventDefinitionCRFDao {
    public EventDefinitionCRFDaoImpl(DataSource ds) { super(ds); }
}

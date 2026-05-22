package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.submit.EventCRFDAO;
import org.researchedc.dao.spi.EventCRFDao;

public class EventCRFDaoImpl extends EventCRFDAO implements EventCRFDao {
    public EventCRFDaoImpl(DataSource ds) { super(ds); }
}

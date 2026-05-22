package org.researchedc.legacy.dao;

import javax.sql.DataSource;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;

public class IDiscrepancyNoteDAOImpl extends DiscrepancyNoteDAO implements IDiscrepancyNoteDAO {

    public IDiscrepancyNoteDAOImpl(DataSource ds) {
        super(ds);
    }
}

/* 
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * OpenClinica is distributed under the
 * Copyright 2003-2008 Akaza Research 
 */
package org.researchedc.service.managestudy;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class DiscrepancyNoteService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    DataSource ds;
    private IDiscrepancyNoteDAO discrepancyNoteDao;

    public DiscrepancyNoteService(DataSource ds) {
        this.ds = ds;
    }

    public DiscrepancyNoteService(IDiscrepancyNoteDAO discrepancyNoteDao) {
        this.discrepancyNoteDao = discrepancyNoteDao;
    }

    public void saveFieldNotes(String description, int entityId, String entityType, StudyBean sb, UserAccountBean ub) {

        // Create a new thread each time
        DiscrepancyNoteBean parent = createDiscrepancyNoteBean(description, entityId, entityType, sb, ub, null);
        createDiscrepancyNoteBean(description, entityId, entityType, sb, ub, parent.getId());

    }

    private DiscrepancyNoteBean createDiscrepancyNoteBean(String description, int entityId, String entityType, StudyBean sb, UserAccountBean ub,
            Integer parentId) {
        DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
        dnb.setEntityId(entityId);
        dnb.setStudyId(sb.getId());
        dnb.setEntityType(entityType);
        dnb.setDescription(description);
        dnb.setDiscrepancyNoteTypeId(1);
        dnb.setResolutionStatusId(1);
        dnb.setColumn("value");
        dnb.setOwner(ub);
        if (parentId != null) {
            dnb.setParentDnId(parentId);
        }
        dnb = (DiscrepancyNoteBean) getDiscrepancyNoteDao().create(dnb);
        getDiscrepancyNoteDao().createMapping(dnb);
        return dnb;

    }

    private IDiscrepancyNoteDAO getDiscrepancyNoteDao() {
        return discrepancyNoteDao;
    }

}

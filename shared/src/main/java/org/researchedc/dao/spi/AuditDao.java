package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;

import java.util.Collection;
import java.util.HashMap;

public interface AuditDao {
    EntityBean findByPK(int id);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findStudySubjectAuditEvents(int studySubjectId);
    Collection findSubjectAuditEvents(int subjectId);
    Object getEntityFromHashMap(HashMap hm);
}

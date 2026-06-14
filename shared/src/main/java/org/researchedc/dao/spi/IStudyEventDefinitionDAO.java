package org.researchedc.dao.spi;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface IStudyEventDefinitionDAO {
    EntityBean findByPK(int ID);
    AuditableEntityBean findByPKAndStudy(int id, StudyBean study);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    Object getEntityFromHashMap(HashMap hm);
    StudyEventDefinitionBean findByOid(String oid);
    StudyEventDefinitionBean findByOidAndStudy(String oid, int studyId, int parentStudyId);
    ArrayList findAllByStudy(StudyBean study);
    ArrayList findAllWithStudyEvent(StudyBean currentStudy);
    ArrayList<StudyEventDefinitionBean> findAllByCrf(CRFBean crf);
    EntityBean findByName(String name);
    StudyEventDefinitionBean findByEventDefinitionCRFId(int eventDefinitionCRFId);
    Collection findAllByStudyAndLimit(int studyId);
    ArrayList<StudyEventDefinitionBean> findAllActiveByParentStudyId(int parentStudyId);
    java.util.ArrayList findAllActiveByStudy(StudyBean study);
}

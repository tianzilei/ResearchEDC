package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.domain.datamap.Study;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public interface IStudyDAO {
    EntityBean findByPK(int ID);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    Object getEntityFromHashMap(HashMap hm);
    Collection findAllByUser(String username);
    Collection findAllByUserNotRemoved(String username);
    ArrayList findAllByStatus(Status status);
    Collection findAllByLimit(boolean isLimited);
    Collection findAllParents();
    boolean isAParent(int studyId);
    Collection findAllByParent(int parentStudyId);
    Collection findAllByParentAndLimit(int parentStudyId, boolean isLimited);
    Collection findAll(int studyId);
    Collection<Integer> findAllSiteIdsByStudy(StudyBean study);
    Collection<Integer> findOlnySiteIdsByStudy(StudyBean study);
    Collection findAllByParentStudyIdOrderedByIdAsc(int parentStudyId);
    StudyBean findByStudySubjectId(int studySubjectId);
    StudyBean updateSitesStatus(StudyBean sb);
    StudyBean updateStudyStatus(StudyBean sb);
    HashMap getChildrenByParentIds(ArrayList allStudies);
    StudyBean findByOid(String oid);
    StudyBean findByUniqueIdentifier(String oid);
    StudyBean findSiteByUniqueIdentifier(String parentUniqueIdentifier, String siteUniqueIdentifier);
    EntityBean findByName(String name);
    void deleteTestOnly(String name);
    ArrayList<Integer> getStudyIdsByCRF(int crfId);
    default Study findByOcOID(String OCOID) { throw new UnsupportedOperationException(); }
    default Study findById(int id) { throw new UnsupportedOperationException(); }
    default Study findByColumnName(Object value, String columnName) { throw new UnsupportedOperationException(); }
}

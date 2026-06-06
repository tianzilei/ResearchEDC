package org.researchedc.dao.spi;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.domain.datamap.CrfBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface ICrfDAO {
    EntityBean findByPK(int ID);
    EntityBean findByName(String name);
    EntityBean findAnotherByName(String name, int crfId);
    CRFBean findByVersionId(int crfVersionId);
    CRFBean findByOid(String oid);
    CRFBean findByItemOid(String itemOid);
    EntityBean create(EntityBean eb);
    EntityBean update(EntityBean eb);
    Collection findAll();
    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByLimit(boolean hasLimit);
    Collection findAllByStudy(int studyId);
    Collection findAllByStatus(Status status);
    Collection findAllActiveByDefinition(StudyEventDefinitionBean definition);
    Collection findAllActiveByDefinitions(int studyId);
    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);
    Collection findAllByPermission(Object objCurrentUser, int intActionType);
    ArrayList<CRFBean> findAllByOid(String oid);
    Integer getCountofActiveCRFs();
    String getValidOid(CRFBean crfBean, String crfName);
    Map<Integer, CRFBean> buildCrfById(Integer studySubjectId);
    default CrfBean findById(int id) { throw new UnsupportedOperationException(); }
    default CrfBean saveOrUpdate(CrfBean entity) { throw new UnsupportedOperationException(); }
    default Serializable save(CrfBean entity) { throw new UnsupportedOperationException(); }
    default String getValidOid(CrfBean crfBean, String crfName) { throw new UnsupportedOperationException(); }
    default CrfBean findByCrfId(Integer crfId) { throw new UnsupportedOperationException(); }
    default CrfBean findByNameEntity(String name) { throw new UnsupportedOperationException(); }
    Object getEntityFromHashMap(HashMap hm);
}

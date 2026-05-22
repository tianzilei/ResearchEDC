package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.domain.datamap.CrfVersion;

public interface ICrfVersionDAO {

    public EntityBean update(EntityBean eb);

    public EntityBean create(EntityBean eb);

    public void setTypesExpected();

    public Object getEntityFromHashMap(HashMap hm);

    public Collection findAll();

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);

    public Collection findAllByCRF(int crfId);

    public Collection findAllActiveByCRF(int crfId);

    public Collection findItemFromMap(int versionId);

    public Collection findItemUsedByOtherVersion(int versionId);

    public ArrayList findNotSharedItemsByVersion(int versionId);

    public ArrayList findDefCRFVersionsByStudyEvent(int studyEventDefinitionId);

    public boolean isItemUsedByOtherVersion(int versionId);

    public boolean hasItemData(int itemId);

    public EntityBean findByPK(int ID);

    public EntityBean findByFullName(String version, String crfName);

    public void delete(int id);

    public ArrayList generateDeleteQueries(int versionId, ArrayList items);

    public String getValidOid(CRFVersionBean crfVersion, String crfName, String crfVersionName);

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);

    public Collection findAllByPermission(Object objCurrentUser, int intActionType);

    public ArrayList findAllByOid(String oid);

    public int getCRFIdFromCRFVersionId(int CRFVersionId);

    public ArrayList findAllByCRFId(int CRFId);

    public Integer findCRFVersionId(int crfId, String versionName);

    public CRFVersionBean findByOid(String oid);

    public Map<Integer, CRFVersionBean> buildCrfVersionById(Integer studySubjectId);
    default org.researchedc.domain.datamap.CrfVersion findByOcOID(String OCOID) { throw new UnsupportedOperationException(); }

}

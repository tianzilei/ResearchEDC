package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.core.util.ItemGroupCrvVersionUtil;

public interface IItemDAO {

    public void setTypesExpected();

    public EntityBean update(EntityBean eb);

    public EntityBean create(EntityBean eb);

    public Integer getCountofActiveItems();

    public String getValidOid(ItemBean itemBean, String crfName, String itemLabel, ArrayList<String> oidList);

    public Object getEntityFromHashMap(HashMap hm);

    public List<ItemBean> findByOid(String oid);

    public Collection findAll();

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);

    public ArrayList findAllParentsBySectionId(int sectionId);

    public ArrayList findAllNonRepeatingParentsBySectionId(int sectionId);

    public ArrayList findAllBySectionId(int sectionId);

    public ArrayList findAllBySectionIdOrderedByItemFormMetadataOrdinal(int sectionId);

    public ArrayList findAllUngroupedParentsBySectionId(int sectionId, int crfVersionId);

    public ArrayList findAllItemsByVersionId(int versionId);

    public ArrayList findAllVersionsByItemId(int itemId);

    public List<ItemBean> findAllItemsByGroupId(int id, int crfVersionId);

    public List<ItemBean> findAllItemsByGroupIdOrdered(int id, int crfVersionId);

    public List<ItemBean> findAllItemsByGroupIdAndSectionIdOrdered(int id, int crfVersionId , int sectionId);

    public List<ItemBean> findAllItemsByGroupIdForPrint(int id, int crfVersionId,int sectionId);

    public ItemBean findItemByGroupIdandItemOid(int id, String itemOid);

    public ArrayList findAllActiveByCRF(CRFBean crf);

    public EntityBean findByPK(int ID);

    public EntityBean findByName(String name);

    public EntityBean findByNameAndCRFId(String name, int crfId);

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);

    public Collection findAllByPermission(Object objCurrentUser, int intActionType);

    public ArrayList findAllByParentIdAndCRFVersionId(int parentId, int crfVersionId);

    public int findAllRequiredByCRFVersionId(int crfVersionId);

    public ArrayList findAllRequiredBySectionId(int sectionId);

    public Map<String,Integer> mapAllItemNameAndItemIdInSection(Integer sectionId);

    public Map<String,String> mapAllChildAndParentNameInSection(Integer sectionId);

    public ArrayList<ItemBean> findAllWithItemDataByCRFVersionId(int crfVersionId,int eventCRFId);

    public ArrayList<ItemGroupCrvVersionUtil> findAllWithItemGroupCRFVersionMetadataByCRFId( String  crfName);

    public ArrayList<ItemGroupCrvVersionUtil> findAllWithItemDetailsGroupCRFVersionMetadataByCRFId( String  crfName);
}

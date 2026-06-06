package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.domain.datamap.CrfBean;
import org.researchedc.domain.datamap.ItemGroup;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.researchedc.bean.submit.ItemGroupBean;

public interface IItemGroupDAO {

    public void setTypesExpected();

    public EntityBean update(EntityBean eb);

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws org.researchedc.exception.OpenClinicaException;

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) throws org.researchedc.exception.OpenClinicaException;

    public String getValidOid(ItemGroupBean itemGroup, String crfName, String itemGroupLabel, ArrayList<String> oidList);

    public EntityBean create(EntityBean eb);

    public Collection findAll();

    public Collection findGroupsByItemID(int ID);

    public List<ItemGroupBean> findGroupByCRFVersionIDMap(int Id);

    public EntityBean findByPK(int ID);

    public EntityBean findByName(String name);

    public List<ItemGroupBean> findAllByOid(String oid);

    public ItemGroupBean findByOid(String oid);

    public ItemGroupBean findByOidAndCrf(String oid, int crfId);

    public List<ItemGroupBean> findGroupByCRFVersionID(int Id);

    public ItemGroupBean findGroupByGroupNameAndCrfVersionId(String groupName, int crfVersionId);

    public ItemGroupBean findGroupByItemIdCrfVersionId(int itemId, int crfVersionId);

    public List<ItemGroupBean> findOnlyGroupsByCRFVersionID(int Id);

    public List<ItemGroupBean> findGroupBySectionId(int sectionId);

    public List<ItemGroupBean> findLegitGroupBySectionId(int sectionId);

    public List<ItemGroupBean> findLegitGroupAllBySectionId(int sectionId);

    public Object getEntityFromHashMap(HashMap hm);

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);

    public void deleteTestGroup(String name);

    public Boolean isItemGroupRepeatingBasedOnAllCrfVersions(String groupOid);

    public Boolean isItemGroupRepeatingBasedOnCrfVersion(String groupOid,Integer crfVersion);

    public ItemGroupBean findTopOneGroupBySectionId(int sectionId);

    default ItemGroup findByNameCrfId(String name, CrfBean crf) { throw new UnsupportedOperationException(); }
    default ItemGroup findByNameCrfId(String name, int crfId) { throw new UnsupportedOperationException(); }
    default java.io.Serializable save(ItemGroup entity) { throw new UnsupportedOperationException(); }
    default String getValidOid(ItemGroup itemGroup, String crfName, String itemGroupLabel, java.util.ArrayList<String> oidList) { throw new UnsupportedOperationException(); }
    default ArrayList<ItemGroup> findByCrfVersionId(Integer crfVersionId) { throw new UnsupportedOperationException(); }
}

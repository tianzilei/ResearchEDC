package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.researchedc.bean.core.ItemDataType;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.domain.datamap.ItemData;

public interface IItemDataDAO {

    public boolean isFormatDates();

    public void setFormatDates(boolean formatDates);

    public Collection findMinMaxDates();

    public void setTypesExpected();

    public void setExtraTypesExpected();

    public HashMap findByStudySubjectAndOids(Integer studyId, String itemOid, String itemGroupOid,int studySubjectId);

    public void setExtraTypesExpectedForStudyLevelSql();

    public EntityBean update(EntityBean eb);

    public EntityBean updateValue(EntityBean eb);

    public EntityBean updateValueForRemoved(EntityBean eb);

    public EntityBean updateStatus(EntityBean eb);

    public ItemDataBean setItemDataBeanIfDateOrPdate(ItemDataBean idb, String current_df_string, ItemDataType dataType);

    public EntityBean updateValue(EntityBean eb, String current_df_string);

    public EntityBean updateUser(EntityBean eb);

    public EntityBean create(EntityBean eb);

    public EntityBean upsert(EntityBean eb);

    public ItemDataType getDataType(int itemId);

    public String formatPDate(String pDate);

    public String reFormatPDate(String pDate);

    public Object getEntityFromHashMap(HashMap hm);

    public Object getKeyFromHashMap(HashMap hm);

    public List<ItemDataBean> findByStudyEventAndOids(Integer studyEventId, String itemOid, String itemGroupOid);

    public HashMap findCountByStudyEventAndOIDs(Integer studyId, String itemOid, String itemGroupOid);

    public Collection<ItemDataBean> findAll();

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);

    public EntityBean findByPK(int ID);

    public void delete(int itemDataId);

    public void deleteDnMap(int itemDataId);

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase);

    public Collection findAllByPermission(Object objCurrentUser, int intActionType);

    public ArrayList<ItemDataBean> findAllBySectionIdAndEventCRFId(int sectionId, int eventCRFId);

    public ArrayList<ItemDataBean> findByCRFVersion(CRFVersionBean crfVersionBean);

    public ArrayList<ItemDataBean> findAllActiveBySectionIdAndEventCRFId(int sectionId, int eventCRFId);

    default List<ItemData> findByEventCrfId(int eventCRFId) { throw new UnsupportedOperationException(); }
    public ArrayList<ItemDataBean> findAllByEventCRFId(int eventCRFId);

    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemId(int eventCRFId, int itemId);

    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemIdNoStatus(int eventCRFId, int itemId);

    public ArrayList<ItemDataBean> findAllBlankRequiredByEventCRFId(int eventCRFId, int crfVersionId);

    public ItemDataBean findByEventCRFIdAndItemName(EventCRFBean eventCrfBean, String itemName);

    public void updateStatusByEventCRF(EventCRFBean eventCRF, Status s);

    public ItemDataBean findByItemIdAndEventCRFId(int itemId, int eventCRFId);

    public ItemDataBean findByItemIdAndEventCRFIdAndOrdinal(int itemId, int eventCRFId, int ordinal);

    public ItemDataBean findByItemIdAndEventCRFIdAndOrdinalRaw(int itemId, int eventCRFId, int ordinal);

    public int findAllRequiredByEventCRFId(EventCRFBean ecb);

    public int getMaxOrdinalForGroup(EventCRFBean ecb, SectionBean sb, ItemGroupBean igb);

    public int getMaxOrdinalForGroupByGroupOID(String item_group_oid, int event_crf_id);

    public int getMaxOrdinalForGroupByItemAndEventCrf(Integer itemId, EventCRFBean ec);

    public boolean isItemExists(int item_id, int ordinal_for_repeating_group_field, int event_crf_id);

    public int getGroupSize(int itemId, int eventcrfId);

    public List<String> findValuesByItemOID(String itoid);

    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemGroupId(int eventCRFId, int itemGroupId);

    public void undelete(int itemDataId, int updaterId);

    default ItemData findByItemEventCrfOrdinal(Integer itemId, Integer eventCrfId, Integer ordinal) { throw new UnsupportedOperationException(); }
    default int getMaxGroupRepeat(Integer eventCrfId, Integer itemId) { throw new UnsupportedOperationException(); }
    default ItemData saveOrUpdate(ItemData entity) { throw new UnsupportedOperationException(); }
    default List<ItemData> findAllByEventCrf(Integer eventCrfId) { throw new UnsupportedOperationException(); }
    default List<ItemData> findByEventCrfGroup(Integer eventCrfId, Integer itemGroupId) { throw new UnsupportedOperationException(); }
}

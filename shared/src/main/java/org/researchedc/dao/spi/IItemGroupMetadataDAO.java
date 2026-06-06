package org.researchedc.dao.spi;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.domain.datamap.ItemGroupMetadata;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.researchedc.bean.submit.ItemGroupMetadataBean;
import org.researchedc.exception.OpenClinicaException;

public interface IItemGroupMetadataDAO {

    public void setTypesExpected();

    public Object getEntityFromHashMap(HashMap hm);

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws org.researchedc.exception.OpenClinicaException;

    public Collection findAll() throws org.researchedc.exception.OpenClinicaException;

    public EntityBean findByPK(int id) throws org.researchedc.exception.OpenClinicaException;

    public EntityBean findByItemAndCrfVersion(Integer itemId, Integer crfVersionId);

    public EntityBean create(EntityBean eb) throws org.researchedc.exception.OpenClinicaException;

    public List<ItemGroupMetadataBean> findMetaByGroupAndSection(int itemGroupId, int crfVersionId, int sectionId);

    public List<ItemGroupMetadataBean> findMetaByGroupAndCrfVersion(int itemGroupId, int crfVersionId);

    public List<ItemGroupMetadataBean> findMetaByGroupAndSectionForPrint(int itemGroupId, int crfVersionId, int sectionId);

    public EntityBean update(EntityBean eb) throws org.researchedc.exception.OpenClinicaException;

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws org.researchedc.exception.OpenClinicaException;

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) throws org.researchedc.exception.OpenClinicaException;

    public boolean versionIncluded(int crfVersionId);

    public List<ItemGroupMetadataBean> findByCrfVersion(Integer crfVersionId);

    default ItemGroupMetadata saveOrUpdate(ItemGroupMetadata entity) { throw new UnsupportedOperationException(); }
    default ArrayList<ItemGroupMetadata> findByItemGroupCrfVersion(Integer itemGroupId, Integer crfVersionId) { throw new UnsupportedOperationException(); }
    default ItemGroupMetadata findByItemGroupCrfVersionOrdered(int item_id, int crf_version_id) { throw new UnsupportedOperationException(); }
    default List<ItemGroupMetadata> findAllByCrfVersion(int crfVersionId) { throw new UnsupportedOperationException(); }
}

package org.researchedc.dao.spi;

import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.domain.crfdata.DynamicsItemFormMetadataBean;

import java.util.ArrayList;
import java.util.List;

public interface DynamicsItemFormMetadataDao {

    DynamicsItemFormMetadataBean findByMetadataBean(ItemFormMetadataBean metadataBean, EventCRFBean eventCrfBean,
            ItemDataBean itemDataBean);

    ArrayList<DynamicsItemFormMetadataBean> findByItemAndEventCrfShown(EventCRFBean eventCrfBean, int itemId);

    DynamicsItemFormMetadataBean findByItemDataBean(ItemDataBean itemDataBean);

    List<Integer> findItemIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId);

    List<Integer> findShowItemIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId);

    List<Integer> findShowItemDataIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId);

    List<Integer> findHideItemDataIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId);

    List<Integer> findShowItemDataIdsInSection(int sectionId, int crfVersionId, int eventCrfId);

    Boolean hasShowingInSection(int sectionId, int crfVersionId, int eventCrfId);

    void delete(int eventCrfId);

    default DynamicsItemFormMetadataBean saveOrUpdate(DynamicsItemFormMetadataBean entity) {
        throw new UnsupportedOperationException();
    }

}

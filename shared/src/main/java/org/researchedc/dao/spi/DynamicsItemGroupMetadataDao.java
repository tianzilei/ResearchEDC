package org.researchedc.dao.spi;

import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemGroupMetadataBean;
import org.researchedc.domain.crfdata.DynamicsItemGroupMetadataBean;

public interface DynamicsItemGroupMetadataDao {

    DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, EventCRFBean eventCrfBean);

    DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, int eventCrfBeanId);

    Boolean hasShowingInSection(int sectionId, int crfVersionId, int eventCrfId);

    void delete(int eventCrfId);

    default DynamicsItemGroupMetadataBean saveOrUpdate(DynamicsItemGroupMetadataBean entity) {
        throw new UnsupportedOperationException();
    }

}

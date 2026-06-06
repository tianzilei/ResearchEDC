package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.EventCrfFlag;

public interface EventCrfFlagDao {

    EventCrfFlag findByEventCrfPath(int tagId, String path);

    default EventCrfFlag findById(Integer id) {
        throw new UnsupportedOperationException();
    }

    default EventCrfFlag saveOrUpdate(EventCrfFlag eventCrfFlag) {
        throw new UnsupportedOperationException();
    }

}

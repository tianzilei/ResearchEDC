package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.EventDefinitionCrfTag;

public interface EventDefinitionCrfTagDao {

    EventDefinitionCrfTag findByCrfPath(int tagId, String path, boolean active);

    default EventDefinitionCrfTag saveOrUpdate(EventDefinitionCrfTag entity) {
        throw new UnsupportedOperationException();
    }

    default EventDefinitionCrfTag findById(int id) {
        throw new UnsupportedOperationException();
    }

}

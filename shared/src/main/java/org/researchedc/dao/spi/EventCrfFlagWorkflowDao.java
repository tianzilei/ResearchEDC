package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.EventCrfFlagWorkflow;

public interface EventCrfFlagWorkflowDao {

    default EventCrfFlagWorkflow findById(Integer id) {
        throw new UnsupportedOperationException();
    }

    default EventCrfFlagWorkflow saveOrUpdate(EventCrfFlagWorkflow eventCrfFlagWorkflow) {
        throw new UnsupportedOperationException();
    }

}

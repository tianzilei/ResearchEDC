package org.researchedc.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.domain.datamap.EventCrfFlag;
import org.researchedc.domain.datamap.EventCrfFlagWorkflow;
import org.researchedc.domain.datamap.EventDefinitionCrfTag;
import org.researchedc.domain.datamap.EventDefinitionCrfItemTag;
import org.researchedc.domain.datamap.ItemDataFlag;
import org.researchedc.domain.datamap.ItemData;

public class EventCrfFlagWorkflowDao extends AbstractDomainDao<EventCrfFlagWorkflow> {

    @Override
    Class<EventCrfFlagWorkflow> domainClass() {
        // TODO Auto-generated method stub
        return EventCrfFlagWorkflow.class;
    }


}

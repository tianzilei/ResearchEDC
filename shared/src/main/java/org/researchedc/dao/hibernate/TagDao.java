package org.researchedc.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.domain.datamap.ItemData;
import org.researchedc.domain.datamap.EventDefinitionCrfItemTag;
import org.researchedc.domain.datamap.Tag;

public class TagDao extends AbstractDomainDao<Tag> {

    @Override
    Class<Tag> domainClass() {
        // TODO Auto-generated method stub
        return Tag.class;
    }

    
}

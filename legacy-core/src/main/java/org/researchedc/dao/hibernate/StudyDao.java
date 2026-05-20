package org.researchedc.dao.hibernate;

import org.researchedc.domain.datamap.Study;


public class StudyDao extends AbstractDomainDao<Study> {
	
    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }
}

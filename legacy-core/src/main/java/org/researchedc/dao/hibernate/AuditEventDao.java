package org.researchedc.dao.hibernate;

import org.researchedc.domain.datamap.AuditEvent;


public class AuditEventDao extends AbstractDomainDao<AuditEvent> {

	 @Override
	    public Class<AuditEvent> domainClass() {
	        return AuditEvent.class;
	    }
}

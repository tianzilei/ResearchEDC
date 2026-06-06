package org.researchedc.dao.spi;

import org.researchedc.domain.datamap.AuditLogEvent;

public interface AuditLogEventDao {

    <T> T findByParam(AuditLogEvent auditLogEvent, String anotherAuditTable);

}

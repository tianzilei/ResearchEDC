package org.researchedc.module.audit.service;

import org.researchedc.module.audit.dto.AuditUserLoginDTO;
import org.researchedc.module.audit.dto.AuditUserLoginQuery;
import org.springframework.data.domain.Page;

public interface AuditUserLoginPort {

    Page<AuditUserLoginDTO> findUserLogins(AuditUserLoginQuery query);
}

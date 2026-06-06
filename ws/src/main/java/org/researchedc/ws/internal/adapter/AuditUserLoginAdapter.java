package org.researchedc.ws.internal.adapter;

import org.researchedc.dao.spi.AuditUserLoginDao;
import org.researchedc.domain.technicaladmin.AuditUserLoginBean;
import org.springframework.stereotype.Repository;

@Repository
public class AuditUserLoginAdapter {

    private final AuditUserLoginDao delegate;

    public AuditUserLoginAdapter(AuditUserLoginDao delegate) {
        this.delegate = delegate;
    }

    public AuditUserLoginBean saveOrUpdate(AuditUserLoginBean bean) {
        return delegate.saveOrUpdate(bean);
    }

    public AuditUserLoginDao getDelegate() {
        return delegate;
    }
}

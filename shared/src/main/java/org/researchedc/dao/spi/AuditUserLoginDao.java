package org.researchedc.dao.spi;

import org.researchedc.dao.hibernate.AuditUserLoginFilter;
import org.researchedc.dao.hibernate.AuditUserLoginSort;
import org.researchedc.domain.technicaladmin.AuditUserLoginBean;

import java.util.ArrayList;

public interface AuditUserLoginDao {

    int getCountWithFilter(AuditUserLoginFilter filter);

    ArrayList<AuditUserLoginBean> getWithFilterAndSort(AuditUserLoginFilter filter, AuditUserLoginSort sort, int rowStart, int rowEnd);

}

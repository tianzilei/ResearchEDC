package org.researchedc.module.audit.internal.adapter;

import java.util.List;

import org.researchedc.dao.hibernate.AuditUserLoginFilter;
import org.researchedc.dao.hibernate.AuditUserLoginSort;
import org.researchedc.domain.technicaladmin.AuditUserLoginBean;
import org.researchedc.module.audit.dto.AuditUserLoginDTO;
import org.researchedc.module.audit.dto.AuditUserLoginQuery;
import org.researchedc.module.audit.service.AuditUserLoginPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
class AuditUserLoginAdapter implements AuditUserLoginPort {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "loginAttemptDate");

    private final org.researchedc.dao.spi.AuditUserLoginDao auditUserLoginDao;

    AuditUserLoginAdapter(org.researchedc.dao.spi.AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

    @Override
    public Page<AuditUserLoginDTO> findUserLogins(AuditUserLoginQuery query) {
        AuditUserLoginFilter filter = toFilter(query);
        int total = auditUserLoginDao.getCountWithFilter(filter);
        Pageable pageable = query.pageable();
        int rowStart = (int) pageable.getOffset();
        int rowEnd = rowStart + pageable.getPageSize();
        List<AuditUserLoginDTO> rows = auditUserLoginDao.getWithFilterAndSort(filter, toSort(pageable), rowStart, rowEnd)
                .stream()
                .map(this::toDto)
                .toList();
        return new PageImpl<>(rows, pageable, total);
    }

    private AuditUserLoginFilter toFilter(AuditUserLoginQuery query) {
        AuditUserLoginFilter filter = new AuditUserLoginFilter();
        addFilter(filter, "userName", query.userName());
        addFilter(filter, "loginAttemptDate", query.loginAttemptDate());
        addFilter(filter, "loginStatus", query.loginStatus());
        addFilter(filter, "details", query.details());
        return filter;
    }

    private void addFilter(AuditUserLoginFilter filter, String property, String value) {
        if (value != null && !value.isBlank()) {
            filter.addFilter(property, value);
        }
    }

    private AuditUserLoginSort toSort(Pageable pageable) {
        AuditUserLoginSort sort = new AuditUserLoginSort();
        Sort requestedSort = pageable.getSort().isSorted() ? pageable.getSort() : DEFAULT_SORT;
        requestedSort.forEach(order -> sort.addSort(order.getProperty(), order.isAscending() ? "asc" : "desc"));
        return sort;
    }

    private AuditUserLoginDTO toDto(AuditUserLoginBean bean) {
        return new AuditUserLoginDTO(
                bean.getId(),
                bean.getUserName(),
                bean.getUserAccountId(),
                bean.getLoginAttemptDate() != null ? bean.getLoginAttemptDate().toInstant().toString() : null,
                bean.getLoginStatus() != null ? bean.getLoginStatus().name() : null,
                bean.getLoginStatus() != null ? bean.getLoginStatus().getCode().toString() : null,
                bean.getDetails());
    }
}

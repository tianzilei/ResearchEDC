package org.researchedc.module.audit.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.researchedc.domain.technicaladmin.AuditUserLoginBean;
import org.researchedc.domain.technicaladmin.LoginStatus;
import org.researchedc.module.audit.dto.AuditUserLoginDTO;
import org.researchedc.module.audit.dto.AuditUserLoginQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class AuditUserLoginAdapterTest {

    @Test
    void findUserLogins_mapsLegacyRowsToPagedDtos() {
        org.researchedc.dao.spi.AuditUserLoginDao dao =
                org.mockito.Mockito.mock(org.researchedc.dao.spi.AuditUserLoginDao.class);
        AuditUserLoginBean bean = new AuditUserLoginBean();
        bean.setId(42);
        bean.setUserName("sysadmin");
        bean.setUserAccountId(7);
        bean.setLoginAttemptDate(Date.from(Instant.parse("2026-06-07T12:00:00Z")));
        bean.setLoginStatus(LoginStatus.SUCCESSFUL_LOGIN);
        bean.setDetails("login ok");
        when(dao.getCountWithFilter(any())).thenReturn(41);
        when(dao.getWithFilterAndSort(any(), any(), eq(20), eq(40)))
                .thenReturn(new ArrayList<>(java.util.List.of(bean)));

        AuditUserLoginQuery query = new AuditUserLoginQuery(
                "sys", "2026-06-07", "SUCCESSFUL_LOGIN", "login", PageRequest.of(
                        1, 20, Sort.by(Sort.Direction.DESC, "loginAttemptDate")));
        Page<AuditUserLoginDTO> result = new AuditUserLoginAdapter(dao).findUserLogins(query);

        assertEquals(41, result.getTotalElements());
        assertEquals(1, result.getNumber());
        AuditUserLoginDTO dto = result.getContent().getFirst();
        assertEquals(42, dto.id());
        assertEquals("sysadmin", dto.userName());
        assertEquals(7, dto.userAccountId());
        assertEquals("2026-06-07T12:00:00Z", dto.loginAttemptDate());
        assertEquals("SUCCESSFUL_LOGIN", dto.loginStatus());
        assertEquals("1", dto.loginStatusCode());
        assertEquals("login ok", dto.details());
    }
}

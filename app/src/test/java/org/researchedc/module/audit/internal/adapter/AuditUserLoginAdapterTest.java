package org.researchedc.module.audit.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.researchedc.module.audit.dto.AuditUserLoginDTO;
import org.researchedc.module.audit.dto.AuditUserLoginQuery;
import org.researchedc.module.audit.entity.AuditLoginStatus;
import org.researchedc.module.audit.entity.AuditUserLoginEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class AuditUserLoginAdapterTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findUserLogins_mapsLegacyRowsToPagedDtos() {
        AuditUserLoginEntry bean = new AuditUserLoginEntry();
        bean.setId(42);
        bean.setUserName("sysadmin");
        bean.setUserAccountId(7);
        bean.setLoginAttemptDate(Date.from(Instant.parse("2026-06-07T12:00:00Z")));
        bean.setLoginStatusCode(AuditLoginStatus.SUCCESSFUL_LOGIN.code());
        bean.setDetails("login ok");

        EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
        CriteriaBuilder cb = org.mockito.Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<Long> countQuery = org.mockito.Mockito.mock(CriteriaQuery.class);
        CriteriaQuery<AuditUserLoginEntry> rowQuery = org.mockito.Mockito.mock(CriteriaQuery.class);
        Root<AuditUserLoginEntry> countRoot = org.mockito.Mockito.mock(Root.class);
        Root<AuditUserLoginEntry> rowRoot = org.mockito.Mockito.mock(Root.class);
        Predicate predicate = org.mockito.Mockito.mock(Predicate.class);
        Order order = org.mockito.Mockito.mock(Order.class);
        TypedQuery<Long> typedCount = org.mockito.Mockito.mock(TypedQuery.class);
        TypedQuery<AuditUserLoginEntry> typedRows = org.mockito.Mockito.mock(TypedQuery.class);
        Path stringPath = org.mockito.Mockito.mock(Path.class);
        Expression lowerExpression = org.mockito.Mockito.mock(Expression.class);
        Path datePath = org.mockito.Mockito.mock(Path.class);
        Path statusPath = org.mockito.Mockito.mock(Path.class);
        Expression<Long> countExpression = org.mockito.Mockito.mock(Expression.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(cb.createQuery(AuditUserLoginEntry.class)).thenReturn(rowQuery);
        when(countQuery.from(AuditUserLoginEntry.class)).thenReturn(countRoot);
        when(rowQuery.from(AuditUserLoginEntry.class)).thenReturn(rowRoot);
        when(countRoot.get("userName")).thenReturn(stringPath);
        when(countRoot.get("details")).thenReturn(stringPath);
        when(countRoot.get("loginStatusCode")).thenReturn(statusPath);
        when(countRoot.get("loginAttemptDate")).thenReturn(datePath);
        when(rowRoot.get("userName")).thenReturn(stringPath);
        when(rowRoot.get("details")).thenReturn(stringPath);
        when(rowRoot.get("loginStatusCode")).thenReturn(statusPath);
        when(rowRoot.get("loginAttemptDate")).thenReturn(datePath);
        when(stringPath.as(String.class)).thenReturn(stringPath);
        when(cb.lower(stringPath)).thenReturn(lowerExpression);
        when(cb.like(lowerExpression, "%sys%")).thenReturn(predicate);
        when(cb.like(lowerExpression, "%login%")).thenReturn(predicate);
        when(cb.equal(statusPath, AuditLoginStatus.SUCCESSFUL_LOGIN.code())).thenReturn(predicate);
        when(cb.between(org.mockito.Mockito.eq(datePath), org.mockito.Mockito.any(Date.class), org.mockito.Mockito.any(Date.class)))
                .thenReturn(predicate);
        when(cb.or(org.mockito.Mockito.any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(org.mockito.Mockito.any(Predicate[].class))).thenReturn(predicate);
        when(cb.count(countRoot)).thenReturn(countExpression);
        when(cb.desc(rowRoot.get("loginAttemptDate"))).thenReturn(order);
        when(countQuery.where(predicate)).thenReturn(countQuery);
        when(countQuery.select(countExpression)).thenReturn(countQuery);
        when(rowQuery.where(predicate)).thenReturn(rowQuery);
        when(rowQuery.orderBy(List.of(order))).thenReturn(rowQuery);
        when(entityManager.createQuery(countQuery)).thenReturn(typedCount);
        when(entityManager.createQuery(rowQuery)).thenReturn(typedRows);
        when(typedCount.getSingleResult()).thenReturn(41L);
        when(typedRows.setFirstResult(20)).thenReturn(typedRows);
        when(typedRows.setMaxResults(20)).thenReturn(typedRows);
        when(typedRows.getResultList()).thenReturn(new ArrayList<>(List.of(bean)));

        AuditUserLoginQuery query = new AuditUserLoginQuery(
                "sys", "2026-06-07", "SUCCESSFUL_LOGIN", "login", PageRequest.of(
                        1, 20, Sort.by(Sort.Direction.DESC, "loginAttemptDate")));
        AuditUserLoginAdapter adapter = new AuditUserLoginAdapter();
        adapter.setEntityManager(entityManager);
        Page<AuditUserLoginDTO> result = adapter.findUserLogins(query);

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

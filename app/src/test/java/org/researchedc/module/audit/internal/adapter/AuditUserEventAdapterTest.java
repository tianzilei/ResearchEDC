package org.researchedc.module.audit.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.researchedc.bean.admin.AuditEventBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.spi.IAuditEventDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.module.audit.dto.AuditUserEventDTO;
import org.researchedc.module.audit.dto.AuditUserEventsDTO;
import org.researchedc.i18n.util.ResourceBundleProvider;

class AuditUserEventAdapterTest {

    @BeforeEach
    void setUpLocale() {
        ResourceBundleProvider.updateLocale(Locale.of("us"));
    }

    @Test
    void findUserEvents_mapsLegacyUserAndAuditRows() {
        IAuditEventDAO auditEventDao = org.mockito.Mockito.mock(IAuditEventDAO.class);
        IUserAccountDAO userAccountDao = org.mockito.Mockito.mock(IUserAccountDAO.class);
        UserAccountBean user = new UserAccountBean();
        user.setId(7);
        user.setName("sysadmin");
        user.setFirstName("System");
        user.setLastName("Admin");
        AuditEventBean event = new AuditEventBean();
        event.setId(42);
        event.setAuditDate(Date.from(Instant.parse("2026-06-07T12:00:00Z")));
        event.setAuditTable("user_account");
        event.setUserId(7);
        event.setEntityId(99);
        event.setReasonForChange("updated");
        event.setActionMessage("user_updated");
        event.setColumnName("phone");
        event.setOldValue("old-phone");
        event.setNewValue("new-phone");
        event.setStudyId(11);
        event.setStudyName("Main Study");
        event.setSubjectId(12);
        event.setSubjectName("SUBJ-001");
        HashMap<String, Object> changes = new HashMap<>();
        changes.put("phone", "new-phone");
        event.setChanges(changes);
        when(userAccountDao.findByPK(7)).thenReturn(user);
        when(auditEventDao.findAllByUserId(7)).thenReturn(new ArrayList<>(java.util.List.of(event)));

        AuditUserEventsDTO result = new AuditUserEventAdapter(auditEventDao, userAccountDao).findUserEvents(7);

        assertEquals(7, result.user().id());
        assertEquals("sysadmin", result.user().userName());
        assertEquals("System", result.user().firstName());
        AuditUserEventDTO dto = result.events().getFirst();
        assertEquals(42, dto.id());
        assertEquals("2026-06-07T12:00:00Z", dto.auditDate());
        assertEquals("user_account", dto.auditTable());
        assertEquals(7, dto.userId());
        assertEquals(99, dto.entityId());
        assertEquals("updated", dto.reasonForChange());
        assertEquals("updated", dto.reasonForChangeKey());
        assertEquals("user_updated", dto.actionMessage());
        assertEquals("user_updated", dto.actionMessageKey());
        assertEquals("phone", dto.columnName());
        assertEquals("old-phone", dto.oldValue());
        assertEquals("new-phone", dto.newValue());
        assertEquals(11, dto.studyId());
        assertEquals("Main Study", dto.studyName());
        assertEquals(12, dto.subjectId());
        assertEquals("SUBJ-001", dto.subjectName());
        assertEquals("new-phone", dto.changes().get("phone"));
    }
}

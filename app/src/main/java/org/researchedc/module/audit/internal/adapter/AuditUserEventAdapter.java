package org.researchedc.module.audit.internal.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.researchedc.bean.admin.AuditEventBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.dao.spi.IAuditEventDAO;
import org.researchedc.dao.spi.IUserAccountDAO;
import org.researchedc.module.audit.dto.AuditUserEventDTO;
import org.researchedc.module.audit.dto.AuditUserEventsDTO;
import org.researchedc.module.audit.dto.AuditUserSummaryDTO;
import org.researchedc.module.audit.service.AuditUserEventPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
class AuditUserEventAdapter implements AuditUserEventPort {

    private final IAuditEventDAO auditEventDao;
    private final IUserAccountDAO userAccountDao;

    AuditUserEventAdapter(IAuditEventDAO auditEventDao, IUserAccountDAO userAccountDao) {
        this.auditEventDao = auditEventDao;
        this.userAccountDao = userAccountDao;
    }

    @Override
    public AuditUserEventsDTO findUserEvents(int userId) {
        UserAccountBean user = (UserAccountBean) userAccountDao.findByPK(userId);
        List<AuditUserEventDTO> events = auditEventDao.findAllByUserId(userId)
                .stream()
                .map(event -> toDto((AuditEventBean) event))
                .toList();
        return new AuditUserEventsDTO(toUserSummary(user), events);
    }

    private AuditUserSummaryDTO toUserSummary(UserAccountBean bean) {
        return new AuditUserSummaryDTO(
                bean.getId(),
                bean.getName(),
                bean.getName(),
                bean.getFirstName(),
                bean.getLastName());
    }

    private AuditUserEventDTO toDto(AuditEventBean bean) {
        return new AuditUserEventDTO(
                bean.getId(),
                bean.getAuditDate() != null ? bean.getAuditDate().toInstant().toString() : null,
                bean.getAuditTable(),
                bean.getUserId(),
                bean.getEntityId(),
                bean.getReasonForChange(),
                bean.getReasonForChangeKey(),
                bean.getActionMessage(),
                bean.getActionMessageKey(),
                bean.getColumnName(),
                bean.getOldValue(),
                bean.getNewValue(),
                bean.getStudyId(),
                bean.getStudyName(),
                bean.getSubjectId(),
                bean.getSubjectName(),
                copyMap(bean.getChanges()),
                copyMap(bean.getOtherInfo()));
    }

    private Map<String, Object> copyMap(Map<?, ?> source) {
        Map<String, Object> target = new HashMap<>();
        source.forEach((key, value) -> target.put(String.valueOf(key), value));
        return target;
    }
}

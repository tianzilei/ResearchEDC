package org.researchedc.module.discrepancynote.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.ResolutionStatus;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventBean;
import org.researchedc.dao.spi.IDiscrepancyNoteDAO;
import org.researchedc.module.discrepancynote.entity.DiscrepancyNoteEntity;
import org.researchedc.module.discrepancynote.repository.DiscrepancyNoteRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("discrepancyNoteDAO")
@Primary
@Transactional(readOnly = true)
public class DiscrepancyNoteDaoAdapter implements IDiscrepancyNoteDAO {

    private final DiscrepancyNoteRepository discrepancyNoteRepository;

    public DiscrepancyNoteDaoAdapter(DiscrepancyNoteRepository discrepancyNoteRepository) {
        this.discrepancyNoteRepository = discrepancyNoteRepository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return discrepancyNoteRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(DiscrepancyNoteBean::new);
    }

    @Override
    public Collection findAll() {
        return toBeans(discrepancyNoteRepository.findAll());
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn,
                                          boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllParentsByStudy(StudyBean study) {
        return toBeans(discrepancyNoteRepository.findByStudyIdAndParentDnIdIsNull(study.getId()));
    }

    @Override
    public ArrayList findAllByStudyAndParent(StudyBean study, int parentId) {
        return toBeans(discrepancyNoteRepository.findByStudyIdAndParentDnId(study.getId(), parentId));
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findAllItemNotesByEventCRF(int eventCRFId) {
        return toBeans(discrepancyNoteRepository.findByEntityTypeAndEntityId("itemData", eventCRFId));
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findAllParentItemNotesByEventCRF(int eventCRFId) {
        return toBeans(discrepancyNoteRepository.findByEntityTypeAndEntityIdAndParentDnIdIsNull("itemData", eventCRFId));
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findAllParentItemNotesByEventCRFWithConstraints(int eventCRFId, StringBuffer constraints) {
        return new ArrayList();
    }

    @Override
    public Integer getViewNotesCountWithFilter(String filter, StudyBean currentStudy) {
        return 0;
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findAllDiscrepancyNotesDataByStudy(StudyBean currentStudy) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByEntityAndColumn(String entityName, int entityId, String column) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllEntityByPK(String entityName, int noteId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllSubjectByStudy(StudyBean study) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findAllSubjectByStudyAndId(StudyBean study, int subjectId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllStudySubjectByStudy(StudyBean study) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findAllStudySubjectByStudyAndId(StudyBean study, int studySubjectId) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findAllStudySubjectByStudiesAndStudySubjectId(StudyBean currentStudy,
                                                                                       StudyBean subjectStudy,
                                                                                       int studySubjectId) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findAllSubjectByStudiesAndSubjectId(StudyBean currentStudy,
                                                                             StudyBean subjectStudy,
                                                                             int studySubjectId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllStudyEventByStudy(StudyBean study) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllStudyEventByStudyAndId(StudyBean study, int studySubjectId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllStudyEventByStudiesAndSubjectId(StudyBean currentStudy, StudyBean subjectStudy,
                                                           int studySubjectId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllEventCRFByStudy(StudyBean study) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllEventCRFByStudyAndParent(StudyBean study, DiscrepancyNoteBean parent) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllItemDataByStudy(StudyBean study) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllItemDataByStudyAndParent(StudyBean study, DiscrepancyNoteBean parent) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findItemDataDNotesFromEventCRF(EventCRFBean eventCRFBean) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findEventCRFDNotesFromEventCRF(EventCRFBean eventCRFBean) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findParentItemDataDNotesFromEventCRF(EventCRFBean eventCRFBean) {
        return new ArrayList();
    }

    @Override
    public HashMap<ResolutionStatus, Integer> countByEntityTypeAndStudyEventWithConstraints(String entityType,
                                                                                           StudyEventBean studyEvent,
                                                                                           StringBuffer constraints,
                                                                                           boolean isSite) {
        return new HashMap();
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        DiscrepancyNoteBean bean = (DiscrepancyNoteBean) eb;
        DiscrepancyNoteEntity entity = new DiscrepancyNoteEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(discrepancyNoteRepository.save(entity));
    }

    @Override
    public void createMapping(DiscrepancyNoteBean eb) {
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        DiscrepancyNoteBean bean = (DiscrepancyNoteBean) eb;
        DiscrepancyNoteEntity entity = discrepancyNoteRepository.findById(bean.getId())
                .orElseGet(DiscrepancyNoteEntity::new);
        entity.setDiscrepancyNoteId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        return toBean(discrepancyNoteRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean updateAssignedUser(EntityBean eb) {
        DiscrepancyNoteBean bean = (DiscrepancyNoteBean) eb;
        discrepancyNoteRepository.findById(bean.getId()).ifPresent(entity -> {
            entity.setAssignedUserId(bean.getAssignedUserId());
            discrepancyNoteRepository.save(entity);
        });
        return findByPK(bean.getId());
    }

    @Override
    @Transactional
    public EntityBean updateAssignedUserToNull(EntityBean eb) {
        DiscrepancyNoteBean bean = (DiscrepancyNoteBean) eb;
        discrepancyNoteRepository.findById(bean.getId()).ifPresent(entity -> {
            entity.setAssignedUserId(null);
            discrepancyNoteRepository.save(entity);
        });
        return findByPK(bean.getId());
    }

    @Override
    @Transactional
    public EntityBean updateDnMapActivation(EntityBean eb) {
        return eb;
    }

    @Override
    public boolean isQuerySuccessful() {
        return true;
    }

    @Override
    public ArrayList findAllByParent(DiscrepancyNoteBean parent) {
        return toBeans(discrepancyNoteRepository.findByParentDnId(parent.getId()));
    }

    @Override
    public AuditableEntityBean findEntity(DiscrepancyNoteBean note) {
        return null;
    }

    @Override
    public int findNumExistingNotesForItem(int itemDataId) {
        return 0;
    }

    @Override
    public int findNumOfActiveExistingNotesForItemData(int itemDataId) {
        return 0;
    }

    @Override
    public ArrayList findExistingNotesForItemData(int itemDataId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findExistingNotesForToolTip(int itemDataId) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findParentNotesOnlyByItemData(int itemDataId) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findAllTopNotesByEventCRF(int eventCRFId) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findOnlyParentEventCRFDNotesFromEventCRF(EventCRFBean eventCRFBean) {
        return new ArrayList();
    }

    @Override
    public ArrayList<DiscrepancyNoteBean> findEventCRFDNotesToolTips(EventCRFBean eventCRFBean) {
        return new ArrayList();
    }

    @Override
    public int getResolutionStatusIdForSubjectDNFlag(int subjectId, String column) {
        return 0;
    }

    @Override
    public boolean isFetchMapping() {
        return false;
    }

    @Override
    public void setFetchMapping(boolean fetchMapping) {
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        DiscrepancyNoteEntity entity = new DiscrepancyNoteEntity();
        entity.setDiscrepancyNoteId((Integer) hm.get("discrepancy_note_id"));
        entity.setDescription((String) hm.get("description"));
        entity.setDiscrepancyNoteTypeId((Integer) hm.get("discrepancy_note_type_id"));
        entity.setResolutionStatusId((Integer) hm.get("resolution_status_id"));
        entity.setDetailedNotes((String) hm.get("detailed_notes"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setParentDnId((Integer) hm.get("parent_dn_id"));
        entity.setEntityType((String) hm.get("entity_type"));
        entity.setEntityId((Integer) hm.get("entity_id"));
        entity.setStudyId((Integer) hm.get("study_id"));
        entity.setAssignedUserId((Integer) hm.get("assigned_user_id"));
        return toBean(entity);
    }

    private void apply(DiscrepancyNoteBean bean, DiscrepancyNoteEntity entity) {
        entity.setDescription(bean.getDescription());
        entity.setDiscrepancyNoteTypeId(bean.getDiscrepancyNoteTypeId() > 0 ? bean.getDiscrepancyNoteTypeId() : null);
        entity.setResolutionStatusId(bean.getResolutionStatusId() > 0 ? bean.getResolutionStatusId() : null);
        entity.setDetailedNotes(bean.getDetailedNotes());
        entity.setOwnerId(bean.getOwnerId() > 0 ? bean.getOwnerId() : null);
        entity.setParentDnId(bean.getParentDnId() > 0 ? bean.getParentDnId() : null);
        entity.setEntityType(bean.getEntityType());
        entity.setEntityId(bean.getEntityId() > 0 ? bean.getEntityId() : null);
        entity.setStudyId(bean.getStudyId() > 0 ? bean.getStudyId() : null);
        entity.setAssignedUserId(bean.getAssignedUserId() > 0 ? bean.getAssignedUserId() : null);
    }

    private ArrayList<DiscrepancyNoteBean> toBeans(List<DiscrepancyNoteEntity> entities) {
        ArrayList<DiscrepancyNoteBean> beans = new ArrayList();
        entities.stream()
                .sorted(Comparator.comparing(DiscrepancyNoteEntity::getDiscrepancyNoteId,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private DiscrepancyNoteBean toBean(DiscrepancyNoteEntity entity) {
        DiscrepancyNoteBean bean = new DiscrepancyNoteBean();
        if (entity.getDiscrepancyNoteId() != null) {
            bean.setId(entity.getDiscrepancyNoteId());
        }
        bean.setDescription(entity.getDescription() != null ? entity.getDescription() : "");
        if (entity.getDiscrepancyNoteTypeId() != null) {
            bean.setDiscrepancyNoteTypeId(entity.getDiscrepancyNoteTypeId());
        }
        if (entity.getResolutionStatusId() != null) {
            bean.setResolutionStatusId(entity.getResolutionStatusId());
        }
        bean.setDetailedNotes(entity.getDetailedNotes() != null ? entity.getDetailedNotes() : "");
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        if (entity.getParentDnId() != null) {
            bean.setParentDnId(entity.getParentDnId());
        }
        bean.setEntityType(entity.getEntityType() != null ? entity.getEntityType() : "");
        bean.setEntityId(valueOrZero(entity.getEntityId()));
        bean.setStudyId(valueOrZero(entity.getStudyId()));
        bean.setAssignedUserId(valueOrZero(entity.getAssignedUserId()));
        return bean;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return new Date(0);
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }
}

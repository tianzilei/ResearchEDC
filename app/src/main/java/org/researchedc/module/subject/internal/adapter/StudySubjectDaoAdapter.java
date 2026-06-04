package org.researchedc.module.subject.internal.adapter;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.StudySubjectSDVFilter;
import org.researchedc.dao.StudySubjectSDVSort;
import org.researchedc.dao.managestudy.FindSubjectsFilter;
import org.researchedc.dao.managestudy.FindSubjectsSort;
import org.researchedc.dao.managestudy.ListDiscNotesForCRFFilter;
import org.researchedc.dao.managestudy.ListDiscNotesForCRFSort;
import org.researchedc.dao.managestudy.ListDiscNotesSubjectFilter;
import org.researchedc.dao.managestudy.ListDiscNotesSubjectSort;
import org.researchedc.dao.managestudy.ListEventsForSubjectFilter;
import org.researchedc.dao.managestudy.ListEventsForSubjectSort;
import org.researchedc.dao.managestudy.StudyAuditLogFilter;
import org.researchedc.dao.managestudy.StudyAuditLogSort;
import org.researchedc.dao.spi.IStudySubjectDAO;
import org.researchedc.domain.datamap.Study;
import org.researchedc.domain.datamap.StudySubject;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("studySubjectDAO")
@Primary
@Transactional(readOnly = true)
public class StudySubjectDaoAdapter implements IStudySubjectDAO {

    private final StudySubjectRepository repository;

    public StudySubjectDaoAdapter(StudySubjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(StudySubjectBean::new);
    }

    @Override
    public ArrayList findAllByStudy(StudyBean study) {
        return toBeans(repository.findByStudyId(study.getId()));
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        StudySubjectBean bean = (StudySubjectBean) eb;
        StudySubjectEntity entity = new StudySubjectEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        StudySubjectBean bean = (StudySubjectBean) eb;
        StudySubjectEntity entity = repository.findById(bean.getId())
                .orElseGet(StudySubjectEntity::new);
        entity.setStudySubjectId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Override
    public Collection findAll() {
        return toBeans(repository.findAll());
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
    public Object getEntityFromHashMap(HashMap hm) {
        StudySubjectEntity entity = new StudySubjectEntity();
        entity.setStudySubjectId((Integer) hm.get("study_subject_id"));
        entity.setStudyId((Integer) hm.get("study_id"));
        entity.setSubjectId((Integer) hm.get("subject_id"));
        entity.setLabel((String) hm.get("label"));
        entity.setSecondaryLabel((String) hm.get("secondary_label"));
        entity.setEnrollmentDate(toLocalDateTime((Date) hm.get("enrollment_date")));
        entity.setOcOid((String) hm.get("oc_oid"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        return toBean(entity);
    }

    @Override
    public ArrayList findAllByStudyOrderByLabel(StudyBean sb) {
        return toBeans(repository.findByStudyIdOrderByLabel(sb.getId()));
    }

    @Override
    public ArrayList findAllActiveByStudyOrderByLabel(StudyBean sb) {
        return toBeans(repository.findByStudyIdAndStatusIdOrderByLabel(sb.getId(), Status.AVAILABLE.getId()));
    }

    @Override
    public ArrayList findAllWithStudyEvent(StudyBean currentStudy) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllBySubjectId(int subjectId) {
        return toBeans(repository.findBySubjectId(subjectId));
    }

    @Override
    public EntityBean findAnotherBySameLabel(String label, int studyId, int studySubjectId) {
        return repository.findByLabelAndStudyId(label, studyId).stream()
                .filter(e -> !e.getStudySubjectId().equals(studySubjectId))
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public EntityBean findAnotherBySameLabelInSites(String label, int studyId, int studySubjectId) {
        return repository.findByLabelContainingIgnoreCase(label).stream()
                .filter(e -> !e.getStudySubjectId().equals(studySubjectId))
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public StudySubjectBean findByLabelAndStudy(String label, StudyBean study) {
        return repository.findByLabelAndStudyId(label, study.getId()).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public StudySubjectBean findByLabelAndStudy(String label, Study study) {
        return repository.findByLabelAndStudyId(label, study.getStudyId()).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public StudySubjectBean findSameByLabelAndStudy(String label, int studyId, int id) {
        return repository.findByLabelAndStudyId(label, studyId).stream()
                .filter(e -> !e.getStudySubjectId().equals(id))
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public StudySubjectBean findByOidAndStudy(String oid, int studyId) {
        return repository.findByOcOidAndStudyId(oid, studyId)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public StudySubjectBean findByOid(String oid) {
        return repository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public String findStudySubjectIdsByStudyIds(String studyIds) {
        if (studyIds == null || studyIds.isEmpty()) return "";
        List<StudySubjectEntity> results = new ArrayList<>();
        for (String idStr : studyIds.split(",")) {
            try {
                results.addAll(repository.findByStudyId(Integer.parseInt(idStr.trim())));
            } catch (NumberFormatException ignored) {
            }
        }
        return results.stream()
                .map(e -> String.valueOf(e.getStudySubjectId()))
                .collect(Collectors.joining(","));
    }

    @Override
    public StudySubjectBean findBySubjectIdAndStudy(int subjectId, StudyBean study) {
        return repository.findBySubjectIdAndStudyId(subjectId, study.getId())
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public ArrayList findAllByStudyId(int studyId) {
        return toBeans(repository.findByStudyId(studyId));
    }

    @Override
    public ArrayList findAllByStudyIdAndLimit(int studyId, boolean isLimited) {
        return toBeans(repository.findByStudyId(studyId));
    }

    @Override
    public int findTheGreatestLabel() {
        return repository.findTopByOrderByStudySubjectIdDesc()
                .map(e -> {
                    try {
                        return Integer.parseInt(e.getLabel());
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                })
                .orElse(0);
    }

    @Override
    @Transactional
    public StudySubjectBean create(StudySubjectBean sb, boolean withGroup) {
        return (StudySubjectBean) create((EntityBean) sb);
    }

    @Override
    @Transactional
    public StudySubjectBean createWithGroup(StudySubjectBean sb) {
        return create(sb, true);
    }

    @Override
    @Transactional
    public StudySubjectBean createWithoutGroup(StudySubjectBean sb) {
        return create(sb, false);
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb, Connection con) {
        return update(eb);
    }

    @Override
    public ArrayList getGroupByStudySubject(int studySubjectId, int studyId, int parentStudyId) {
        return new ArrayList();
    }

    @Override
    public Integer getCountWithFilter(ListDiscNotesSubjectFilter filter, StudyBean study) {
        return 0;
    }

    @Override
    public Integer getCountWithFilter(ListDiscNotesForCRFFilter filter, StudyBean study) {
        return 0;
    }

    @Override
    public ArrayList getWithFilterAndSort(StudyBean study, ListDiscNotesForCRFFilter filter,
                                          ListDiscNotesForCRFSort sort, int rowStart, int rowLength) {
        return new ArrayList();
    }

    @Override
    public ArrayList getWithFilterAndSort(StudyBean study, ListDiscNotesSubjectFilter filter,
                                          ListDiscNotesSubjectSort sort, int rowStart, int rowLength) {
        return new ArrayList();
    }

    @Override
    public Integer getCountWithFilter(ListEventsForSubjectFilter filter, StudyBean study) {
        return 0;
    }

    @Override
    public ArrayList getWithFilterAndSort(StudyBean study, ListEventsForSubjectFilter filter,
                                          ListEventsForSubjectSort sort, int rowStart, int rowLength) {
        return new ArrayList();
    }

    @Override
    public Integer getCountWithFilter(FindSubjectsFilter filter, StudyBean study) {
        return 0;
    }

    @Override
    public ArrayList getWithFilterAndSort(StudyBean study, FindSubjectsFilter filter,
                                          FindSubjectsSort sort, int rowStart, int rowEnd) {
        return new ArrayList();
    }

    @Override
    public Integer getCountWithFilter(StudyAuditLogFilter filter, StudyBean study) {
        return 0;
    }

    @Override
    public ArrayList getWithFilterAndSort(StudyBean study, StudyAuditLogFilter filter,
                                          StudyAuditLogSort sort, int rowStart, int rowEnd) {
        return new ArrayList();
    }

    @Override
    public Integer getTotalEventCrfCountForCrfMigration(CRFVersionBean sourceCrfVersionBean,
                                                        CRFVersionBean targetCrfVersionBean,
                                                        ArrayList<String> studyEventDefnlist,
                                                        ArrayList<String> sitelist) {
        return 0;
    }

    @Override
    public Integer getTotalCountStudySubjectForCrfMigration(CRFVersionBean sourceCrfVersionBean,
                                                            CRFVersionBean targetCrfVersionBean,
                                                            ArrayList<String> studyEventDefnlist,
                                                            ArrayList<String> sitelist) {
        return 0;
    }

    @Override
    public StudySubject findByOcOID(String OCOID) {
        return repository.findByOcOid(OCOID)
                .map(e -> new StudySubject(e.getStudySubjectId(), e.getOcOid() != null ? e.getOcOid() : ""))
                .orElse(null);
    }

    @Override
    public StudySubject findByLabelAndStudyOrParentStudy(String label, Study study) {
        return repository.findByLabelAndStudyId(label, study.getStudyId()).stream()
                .findFirst()
                .map(e -> new StudySubject(e.getStudySubjectId(), e.getOcOid() != null ? e.getOcOid() : ""))
                .orElse(null);
    }

    @Override
    public int findTheGreatestLabelByStudy(Integer studyId) {
        return repository.findByStudyId(studyId).stream()
                .map(e -> {
                    try {
                        return Integer.parseInt(e.getLabel());
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);
    }

    @Override
    public ArrayList<StudySubject> findByLabelAndParentStudy(String label, Study parentStudy) {
        List<StudySubjectEntity> entities = repository.findByLabelAndStudyId(label, parentStudy.getStudyId());
        ArrayList<StudySubject> result = new ArrayList<>();
        for (StudySubjectEntity e : entities) {
            result.add(new StudySubject(e.getStudySubjectId(), e.getOcOid() != null ? e.getOcOid() : ""));
        }
        return result;
    }

    @Override
    public StudySubject saveOrUpdate(StudySubject studySubject) {
        return null;
    }

    @Override
    public String getValidOid(StudySubject studySubject, ArrayList<String> oidList) {
        return studySubject.getOcOid();
    }

    @Override
    public Integer getCountofStudySubjectsAtStudyOrSite(StudyBean currentStudy) {
        return (int) repository.countByStudyId(currentStudy.getId());
    }

    @Override
    public Integer getCountofStudySubjectsAtStudy(StudyBean currentStudy) {
        return (int) repository.countByStudyId(currentStudy.getId());
    }

    @Override
    public Integer getCountofStudySubjects(StudyBean currentStudy) {
        return (int) repository.countByStudyId(currentStudy.getId());
    }

    @Override
    public Integer getCountofStudySubjectsBasedOnStatus(StudyBean currentStudy, Status status) {
        return (int) repository.countByStudyIdAndStatusId(currentStudy.getId(), status.getId());
    }

    @Override
    public ArrayList findAllByStudySDV(int studyId, int parentStudyId, StudySubjectSDVFilter filter,
                                      StudySubjectSDVSort sort, int rowStart, int rowEnd) {
        return new ArrayList();
    }

    @Override
    public int countAllByStudySDV(int studyId, int parentStudyId, StudySubjectSDVFilter filter) {
        return 0;
    }

    private void apply(StudySubjectBean bean, StudySubjectEntity entity) {
        entity.setStudyId(bean.getStudyId());
        entity.setSubjectId(bean.getSubjectId());
        entity.setLabel(bean.getLabel());
        entity.setSecondaryLabel(bean.getSecondaryLabel());
        entity.setEnrollmentDate(toLocalDateTime(bean.getEnrollmentDate()));
        entity.setOcOid(bean.getOid());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.AVAILABLE.getId());
    }

    private ArrayList toBeans(List<StudySubjectEntity> entities) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private StudySubjectBean toBean(StudySubjectEntity entity) {
        StudySubjectBean bean = new StudySubjectBean();
        if (entity.getStudySubjectId() != null) {
            bean.setId(entity.getStudySubjectId());
        }
        bean.setStudyId(valueOrZero(entity.getStudyId()));
        bean.setSubjectId(valueOrZero(entity.getSubjectId()));
        bean.setLabel(entity.getLabel() != null ? entity.getLabel() : "");
        bean.setSecondaryLabel(entity.getSecondaryLabel() != null ? entity.getSecondaryLabel() : "");
        bean.setEnrollmentDate(toDate(entity.getEnrollmentDate()));
        bean.setOid(entity.getOcOid());
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
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

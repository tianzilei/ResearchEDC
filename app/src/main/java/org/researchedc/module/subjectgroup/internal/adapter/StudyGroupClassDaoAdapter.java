package org.researchedc.module.subjectgroup.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.GroupClassType;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;
import org.researchedc.dao.spi.StudyGroupClassDao;
import org.researchedc.module.subjectgroup.entity.StudyGroupClassEntity;
import org.researchedc.module.subjectgroup.repository.StudyGroupClassRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("studyGroupClassDao")
@Primary
@Transactional(readOnly = true)
public class StudyGroupClassDaoAdapter implements StudyGroupClassDao {

    private final StudyGroupClassRepository repository;

    public StudyGroupClassDaoAdapter(StudyGroupClassRepository repository) {
        this.repository = repository;
    }

    @Override
    public EntityBean findByPK(int id) {
        return repository.findById(id)
                .map(this::toBean)
                .orElseGet(StudyGroupClassBean::new);
    }

    @Override
    public EntityBean findByStudyId(int studyId) {
        return repository.findByStudyId(studyId).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(StudyGroupClassBean::new);
    }

    @Override
    public Collection findAll() {
        return toBeans(repository.findAll(), false);
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return filterAndSort(repository.findAll(), strOrderByColumn, blnAscendingSort, strSearchPhrase);
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn,
                                          boolean blnAscendingSort, String strSearchPhrase) {
        return findAll(strOrderByColumn, blnAscendingSort, strSearchPhrase);
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return findAll();
    }

    @Override
    public ArrayList findAllByStudy(StudyBean study) {
        return toBeans(repository.findByStudyOrChildStudy(study.getId()), false);
    }

    @Override
    public ArrayList findAllActiveByStudy(StudyBean study) {
        return toBeans(repository.findByStudyOrChildStudyAndStatus(study.getId(), Status.AVAILABLE.getId()), true);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        StudyGroupClassBean bean = (StudyGroupClassBean) eb;
        StudyGroupClassEntity entity = new StudyGroupClassEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        StudyGroupClassBean bean = (StudyGroupClassBean) eb;
        StudyGroupClassEntity entity = repository.findById(bean.getId()).orElseGet(StudyGroupClassEntity::new);
        entity.setStudyGroupClassId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Override
    public int getCurrentPK() {
        return repository.findAll().stream()
                .map(StudyGroupClassEntity::getStudyGroupClassId)
                .filter(id -> id != null)
                .max(Integer::compareTo)
                .orElse(0);
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        StudyGroupClassEntity entity = new StudyGroupClassEntity();
        entity.setStudyGroupClassId((Integer) hm.get("study_group_class_id"));
        entity.setName((String) hm.get("name"));
        entity.setStudyId((Integer) hm.get("study_id"));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setGroupClassTypeId((Integer) hm.get("group_class_type_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setSubjectAssignment((String) hm.get("subject_assignment"));
        return toBean(entity);
    }

    private void apply(StudyGroupClassBean bean, StudyGroupClassEntity entity) {
        entity.setName(bean.getName());
        entity.setStudyId(bean.getStudyId());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
        entity.setGroupClassTypeId(bean.getGroupClassTypeId());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setSubjectAssignment(bean.getSubjectAssignment());
    }

    private ArrayList toBeans(List<StudyGroupClassEntity> entities, boolean markUnselected) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .sorted(Comparator.comparing(StudyGroupClassEntity::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toBean)
                .peek(bean -> {
                    if (markUnselected) {
                        bean.setSelected(false);
                    }
                })
                .forEach(beans::add);
        return beans;
    }


    private ArrayList filterAndSort(List<StudyGroupClassEntity> entities, String orderByColumn, boolean ascending,
                                    String searchPhrase) {
        String search = searchPhrase == null ? "" : searchPhrase.trim().toLowerCase(Locale.ROOT);
        Comparator<StudyGroupClassEntity> comparator = comparatorFor(orderByColumn);
        if (!ascending) {
            comparator = comparator.reversed();
        }

        ArrayList beans = new ArrayList();
        entities.stream()
                .filter(entity -> matches(entity, search))
                .sorted(comparator.thenComparing(StudyGroupClassEntity::getStudyGroupClassId,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private Comparator<StudyGroupClassEntity> comparatorFor(String orderByColumn) {
        String column = orderByColumn == null ? "" : orderByColumn.trim().toLowerCase(Locale.ROOT);
        return switch (column) {
            case "study_group_class_id", "id" -> Comparator.comparing(StudyGroupClassEntity::getStudyGroupClassId,
                    Comparator.nullsLast(Integer::compareTo));
            case "study_id" -> Comparator.comparing(StudyGroupClassEntity::getStudyId,
                    Comparator.nullsLast(Integer::compareTo));
            case "status_id" -> Comparator.comparing(StudyGroupClassEntity::getStatusId,
                    Comparator.nullsLast(Integer::compareTo));
            case "group_class_type_id" -> Comparator.comparing(StudyGroupClassEntity::getGroupClassTypeId,
                    Comparator.nullsLast(Integer::compareTo));
            case "subject_assignment" -> Comparator.comparing(StudyGroupClassEntity::getSubjectAssignment,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(StudyGroupClassEntity::getName,
                    Comparator.nullsLast(String::compareToIgnoreCase));
        };
    }

    private boolean matches(StudyGroupClassEntity entity, String search) {
        if (search.isEmpty()) {
            return true;
        }
        return contains(entity.getName(), search)
                || contains(entity.getSubjectAssignment(), search)
                || contains(entity.getStudyGroupClassId(), search)
                || contains(entity.getStudyId(), search)
                || contains(entity.getStatusId(), search)
                || contains(entity.getGroupClassTypeId(), search);
    }

    private boolean contains(Object value, String search) {
        return value != null && value.toString().toLowerCase(Locale.ROOT).contains(search);
    }

    private StudyGroupClassBean toBean(StudyGroupClassEntity entity) {
        StudyGroupClassBean bean = new StudyGroupClassBean();
        if (entity.getStudyGroupClassId() != null) {
            bean.setId(entity.getStudyGroupClassId());
        }
        bean.setName(entity.getName());
        bean.setStudyId(valueOrZero(entity.getStudyId()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setGroupClassTypeId(valueOrZero(entity.getGroupClassTypeId()));
        bean.setGroupClassTypeName(GroupClassType.get(valueOrZero(entity.getGroupClassTypeId())).getName());
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setSubjectAssignment(entity.getSubjectAssignment());
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

package org.researchedc.module.subjectgroup.internal.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyGroupBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.dao.spi.StudyGroupDao;
import org.researchedc.module.subjectgroup.entity.StudyGroupEntity;
import org.researchedc.module.subjectgroup.repository.StudyGroupRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("studyGroupDao")
@Primary
@Transactional(readOnly = true)
public class StudyGroupDaoAdapter implements StudyGroupDao {

    private final StudyGroupRepository repository;

    public StudyGroupDaoAdapter(StudyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public EntityBean findByPK(int id) {
        return repository.findById(id)
                .map(this::toBean)
                .orElseGet(StudyGroupBean::new);
    }

    @Override
    public EntityBean findByStudyId(int studyId) {
        return repository.findFirstByStudyId(studyId)
                .map(this::toBean)
                .orElseGet(StudyGroupBean::new);
    }

    @Override
    public Collection findAll() {
        return toBeans(repository.findAll());
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
        return toBeans(repository.findByStudyOrChildStudy(study.getId()));
    }

    @Override
    public ArrayList findAllByGroupClass(StudyGroupClassBean group) {
        return toBeans(repository.findByStudyGroupClassId(group.getId()));
    }

    @Override
    public ArrayList getGroupByStudySubject(int studySubjectId, int studyId, int parentStudyId) {
        return toBeans(repository.findByStudySubjectInStudyOrChildStudy(studySubjectId, studyId, parentStudyId));
    }

    @Override
    public StudyGroupBean findByNameAndGroupClassID(String name, int studyGroupClassId) {
        return repository.findByNameAndStudyGroupClassId(name, studyGroupClassId)
                .map(this::toBean)
                .orElseGet(StudyGroupBean::new);
    }

    @Override
    public StudyGroupBean findSubjectStudyGroup(int subjectId, String groupClassName) {
        return repository.findSubjectStudyGroup(subjectId, groupClassName).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(StudyGroupBean::new);
    }

    @Override
    public HashMap findByStudySubject(StudySubjectBean studySubject) {
        HashMap classBeanMap = new HashMap();
        repository.findByStudySubject(studySubject.getId()).stream()
                .findFirst()
                .map(this::toBean)
                .ifPresent(group -> classBeanMap.put(group.getStudyGroupClassId(), group));
        return classBeanMap;
    }

    @Override
    public HashMap findSubjectGroupMaps(int studyId) {
        HashMap subjectGroupMaps = new HashMap();
        for (Object[] row : repository.findSubjectGroupMapRows(studyId)) {
            StudyGroupBean group = toBean(row);
            Integer studySubjectId = numberValue(row[4]);

            ArrayList groupMaps = (ArrayList) subjectGroupMaps.get(studySubjectId);
            if (groupMaps == null) {
                groupMaps = new ArrayList();
            }

            HashMap subjectGroupMap = new HashMap();
            subjectGroupMap.put(group.getStudyGroupClassId(), group);
            groupMaps.add(subjectGroupMap);
            subjectGroupMaps.put(studySubjectId, groupMaps);
        }
        return subjectGroupMaps;
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        StudyGroupBean bean = (StudyGroupBean) eb;
        StudyGroupEntity entity = new StudyGroupEntity();
        apply(bean, entity);
        return toBean(repository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        StudyGroupBean bean = (StudyGroupBean) eb;
        StudyGroupEntity entity = repository.findById(bean.getId()).orElseGet(StudyGroupEntity::new);
        entity.setStudyGroupId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        return toBean(repository.save(entity));
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        StudyGroupEntity entity = new StudyGroupEntity();
        entity.setStudyGroupId((Integer) hm.get("study_group_id"));
        entity.setName((String) hm.get("name"));
        entity.setDescription((String) hm.get("description"));
        entity.setStudyGroupClassId((Integer) hm.get("study_group_class_id"));
        return toBean(entity);
    }

    private void apply(StudyGroupBean bean, StudyGroupEntity entity) {
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setStudyGroupClassId(bean.getStudyGroupClassId());
    }

    private ArrayList toBeans(List<StudyGroupEntity> entities) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .sorted(Comparator.comparing(StudyGroupEntity::getStudyGroupId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private ArrayList filterAndSort(List<StudyGroupEntity> entities, String orderByColumn, boolean ascending,
                                    String searchPhrase) {
        String search = searchPhrase == null ? "" : searchPhrase.trim().toLowerCase(Locale.ROOT);
        Comparator<StudyGroupEntity> comparator = comparatorFor(orderByColumn);
        if (!ascending) {
            comparator = comparator.reversed();
        }

        ArrayList beans = new ArrayList();
        entities.stream()
                .filter(entity -> matches(entity, search))
                .sorted(comparator.thenComparing(StudyGroupEntity::getStudyGroupId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private Comparator<StudyGroupEntity> comparatorFor(String orderByColumn) {
        String column = orderByColumn == null ? "" : orderByColumn.trim().toLowerCase(Locale.ROOT);
        return switch (column) {
            case "name" -> Comparator.comparing(StudyGroupEntity::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "description" -> Comparator.comparing(StudyGroupEntity::getDescription,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "study_group_class_id" -> Comparator.comparing(StudyGroupEntity::getStudyGroupClassId,
                    Comparator.nullsLast(Integer::compareTo));
            default -> Comparator.comparing(StudyGroupEntity::getStudyGroupId, Comparator.nullsLast(Integer::compareTo));
        };
    }

    private boolean matches(StudyGroupEntity entity, String search) {
        if (search.isEmpty()) {
            return true;
        }
        return contains(entity.getName(), search)
                || contains(entity.getDescription(), search)
                || contains(entity.getStudyGroupId(), search)
                || contains(entity.getStudyGroupClassId(), search);
    }

    private boolean contains(Object value, String search) {
        return value != null && value.toString().toLowerCase(Locale.ROOT).contains(search);
    }

    private StudyGroupBean toBean(StudyGroupEntity entity) {
        StudyGroupBean bean = new StudyGroupBean();
        if (entity.getStudyGroupId() != null) {
            bean.setId(entity.getStudyGroupId());
        }
        bean.setName(entity.getName());
        bean.setDescription(entity.getDescription());
        bean.setStudyGroupClassId(valueOrZero(entity.getStudyGroupClassId()));
        return bean;
    }

    private StudyGroupBean toBean(Object[] row) {
        StudyGroupBean bean = new StudyGroupBean();
        bean.setId(valueOrZero(numberValue(row[0])));
        bean.setName((String) row[1]);
        bean.setDescription((String) row[2]);
        bean.setStudyGroupClassId(valueOrZero(numberValue(row[3])));
        return bean;
    }

    private Integer numberValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return (Integer) value;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }
}

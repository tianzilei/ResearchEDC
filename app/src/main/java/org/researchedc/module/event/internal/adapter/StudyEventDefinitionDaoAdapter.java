package org.researchedc.module.event.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.dao.spi.IStudyEventDefinitionDAO;
import org.researchedc.domain.datamap.StudyEventDefinition;
import org.researchedc.module.event.entity.StudyEventDefinitionEntity;
import org.researchedc.module.event.repository.StudyEventDefinitionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("studyEventDefinitionDAO")
@Primary
@Transactional(readOnly = true)
public class StudyEventDefinitionDaoAdapter implements IStudyEventDefinitionDAO {

    private final StudyEventDefinitionRepository repository;

    public StudyEventDefinitionDaoAdapter(StudyEventDefinitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(StudyEventDefinitionBean::new);
    }

    @Override
    public AuditableEntityBean findByPKAndStudy(int id, StudyBean study) {
        return repository.findById(id)
                .filter(e -> e.getStudyId() != null && e.getStudyId().equals(study.getId()))
                .map(this::toBean)
                .orElseGet(StudyEventDefinitionBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        StudyEventDefinitionBean bean = (StudyEventDefinitionBean) eb;
        StudyEventDefinitionEntity entity = new StudyEventDefinitionEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        StudyEventDefinitionBean bean = (StudyEventDefinitionBean) eb;
        StudyEventDefinitionEntity entity = repository.findById(bean.getId())
                .orElseGet(StudyEventDefinitionEntity::new);
        entity.setStudyEventDefinitionId(bean.getId() > 0 ? bean.getId() : null);
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
        StudyEventDefinitionEntity entity = new StudyEventDefinitionEntity();
        entity.setStudyEventDefinitionId((Integer) hm.get("study_event_definition_id"));
        entity.setStudyId((Integer) hm.get("study_id"));
        entity.setName((String) hm.get("name"));
        entity.setDescription((String) hm.get("description"));
        entity.setRepeating((Boolean) hm.get("repeating"));
        entity.setType((String) hm.get("type"));
        entity.setCategory((String) hm.get("category"));
        entity.setOcOid((String) hm.get("oc_oid"));
        entity.setOrdinal((Integer) hm.get("ordinal"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        return toBean(entity);
    }

    @Override
    public StudyEventDefinitionBean findByOid(String oid) {
        return repository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public StudyEventDefinitionBean findByOidAndStudy(String oid, int studyId, int parentStudyId) {
        StudyEventDefinitionBean bean = findByOidAndStudy(oid, studyId);
        if (bean == null) {
            bean = findByOidAndStudy(oid, parentStudyId);
        }
        return bean;
    }

    private StudyEventDefinitionBean findByOidAndStudy(String oid, int studyId) {
        return repository.findByOcOidAndStudyId(oid, studyId)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public ArrayList findAllByStudy(StudyBean study) {
        if (study.getParentStudyId() > 0) {
            return toBeans(repository.findByStudyIdOrderByName(study.getParentStudyId()));
        }
        return toBeans(repository.findByStudyIdOrderByName(study.getId()));
    }

    @Override
    public ArrayList findAllWithStudyEvent(StudyBean currentStudy) {
        return new ArrayList();
    }

    @Override
    public ArrayList<StudyEventDefinitionBean> findAllByCrf(CRFBean crf) {
        return new ArrayList<StudyEventDefinitionBean>();
    }

    @Override
    public EntityBean findByName(String name) {
        List<StudyEventDefinitionEntity> results = repository.findByName(name);
        if (results.isEmpty()) {
            return new StudyEventDefinitionBean();
        }
        return toBean(results.get(0));
    }

    @Override
    public StudyEventDefinitionBean findByEventDefinitionCRFId(int eventDefinitionCRFId) {
        return repository.findByEventDefinitionCRFId(eventDefinitionCRFId)
                .map(this::toBean)
                .orElseGet(StudyEventDefinitionBean::new);
    }

    @Override
    public Collection findAllByStudyAndLimit(int studyId) {
        return toBeans(repository.findByStudyIdOrderByName(studyId));
    }

    @Override
    public ArrayList<StudyEventDefinitionBean> findAllActiveByParentStudyId(int parentStudyId) {
        return toBeans(repository.findByStatusIdAndStudyId(Status.AVAILABLE.getId(), parentStudyId));
    }

    @Override
    public Map<Integer, StudyEventDefinitionBean> findByStudySubject(int studySubjectId) {
        return new HashMap<>();
    }

    @Override
    public Map<Integer, Integer> buildMaxOrdinalByStudyEvent(int studySubjectId) {
        return new HashMap<>();
    }

    @Override
    public ArrayList findAllActiveByStudy(StudyBean study) {
        if (study.getParentStudyId() > 0) {
            return toBeans(repository.findByStatusIdAndStudyId(Status.AVAILABLE.getId(), study.getParentStudyId()));
        }
        return toBeans(repository.findByStatusIdAndStudyId(Status.AVAILABLE.getId(), study.getId()));
    }

    @Override
    public StudyEventDefinition findByStudyEventDefinitionId(int studyEventDefinitionId) {
        throw new UnsupportedOperationException("Use findByPK instead");
    }

    private void apply(StudyEventDefinitionBean bean, StudyEventDefinitionEntity entity) {
        entity.setStudyId(bean.getStudyId());
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setRepeating(bean.isRepeating());
        entity.setType(bean.getType());
        entity.setCategory(bean.getCategory());
        entity.setOcOid(bean.getOid());
        entity.setOrdinal(bean.getOrdinal());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
    }

    private ArrayList toBeans(List<StudyEventDefinitionEntity> entities) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .sorted(Comparator.comparing(StudyEventDefinitionEntity::getStudyEventDefinitionId,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private StudyEventDefinitionBean toBean(StudyEventDefinitionEntity entity) {
        StudyEventDefinitionBean bean = new StudyEventDefinitionBean();
        if (entity.getStudyEventDefinitionId() != null) {
            bean.setId(entity.getStudyEventDefinitionId());
        }
        bean.setStudyId(valueOrZero(entity.getStudyId()));
        bean.setName(entity.getName());
        bean.setDescription(entity.getDescription());
        bean.setRepeating(entity.getRepeating() != null && entity.getRepeating());
        bean.setType(entity.getType());
        bean.setCategory(entity.getCategory());
        bean.setOid(entity.getOcOid());
        bean.setOrdinal(valueOrZero(entity.getOrdinal()));
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

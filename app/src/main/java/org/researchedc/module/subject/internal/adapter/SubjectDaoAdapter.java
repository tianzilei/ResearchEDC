package org.researchedc.module.subject.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.SubjectBean;
import org.researchedc.dao.spi.ISubjectDAO;
import org.researchedc.module.subject.entity.SubjectEntity;
import org.researchedc.module.subject.repository.SubjectRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("subjectDAO")
@Primary
@Transactional(readOnly = true)
public class SubjectDaoAdapter implements ISubjectDAO {

    private final SubjectRepository subjectRepository;

    public SubjectDaoAdapter(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return subjectRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(SubjectBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        SubjectBean bean = (SubjectBean) eb;
        SubjectEntity entity = new SubjectEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(subjectRepository.save(entity));
    }

    @Override
    @Transactional
    public SubjectBean create(SubjectBean sb) {
        SubjectEntity entity = new SubjectEntity();
        apply(sb, entity);
        entity.setDateCreated(LocalDateTime.now());
        return (SubjectBean) toBean(subjectRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        SubjectBean bean = (SubjectBean) eb;
        SubjectEntity entity = subjectRepository.findById(bean.getId())
                .orElseGet(SubjectEntity::new);
        entity.setSubjectId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(subjectRepository.save(entity));
    }

    @Override
    public Collection findAll() {
        return toBeans(subjectRepository.findByStatusId(Status.AVAILABLE.getId()));
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByLimit(boolean hasLimit) {
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
    public Collection findAllChildrenByPK(int subjectId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllSubjectsAndStudies() {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllByGender(char gender) {
        return toBeans(subjectRepository.findByGender(String.valueOf(gender)));
    }

    @Override
    public ArrayList findAllMales() {
        return toBeans(subjectRepository.findByGender("m"));
    }

    @Override
    public ArrayList findAllFemales() {
        return toBeans(subjectRepository.findByGender("f"));
    }

    @Override
    public ArrayList findAllByGenderNotSelf(char gender, int id) {
        return toBeans(subjectRepository.findByGenderAndSubjectIdNot(String.valueOf(gender), id));
    }

    @Override
    public ArrayList findAllMalesNotSelf(int id) {
        return toBeans(subjectRepository.findByGenderAndSubjectIdNot("m", id));
    }

    @Override
    public ArrayList findAllFemalesNotSelf(int id) {
        return toBeans(subjectRepository.findByGenderAndSubjectIdNot("f", id));
    }


    @Override
    public EntityBean findAnotherByIdentifier(String name, int subjectId) {
        return findByUniqueIdentifier(name);
    }

    @Override
    public SubjectBean findByUniqueIdentifier(String uniqueIdentifier) {
        return subjectRepository.findByUniqueIdentifier(uniqueIdentifier)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public SubjectBean findByUniqueIdentifierAndAnyStudy(String uniqueIdentifier, int studyId) {
        return subjectRepository.findByUniqueIdentifierAndAnyStudyNative(uniqueIdentifier, studyId)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public SubjectBean findByUniqueIdentifierAndStudy(String uniqueIdentifier, int studyId) {
        return subjectRepository.findByUniqueIdentifierAndStudyNative(uniqueIdentifier, studyId)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public SubjectBean findByUniqueIdentifierAndParentStudy(String uniqueIdentifier, int studyId) {
        return subjectRepository.findByUniqueIdentifierAndParentStudyNative(uniqueIdentifier, studyId)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public void deleteTestSubject(String uniqueIdentifier) {
        // no-op
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        SubjectEntity entity = new SubjectEntity();
        entity.setSubjectId((Integer) hm.get("subject_id"));
        entity.setUniqueIdentifier((String) hm.get("unique_identifier"));
        entity.setDateOfBirth(toLocalDateTime((Date) hm.get("date_of_birth")));
        entity.setGender((String) hm.get("gender"));
        entity.setDobCollected((Boolean) hm.get("dob_collected"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        return toBean(entity);
    }

    private void apply(SubjectBean bean, SubjectEntity entity) {
        entity.setUniqueIdentifier(bean.getUniqueIdentifier());
        entity.setDateOfBirth(toLocalDateTime(bean.getDateOfBirth()));
        entity.setGender(String.valueOf(bean.getGender()));
        entity.setDobCollected(bean.isDobCollected());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
    }

    private ArrayList toBeans(List<SubjectEntity> entities) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private SubjectBean toBean(SubjectEntity entity) {
        SubjectBean bean = new SubjectBean();
        if (entity.getSubjectId() != null) {
            bean.setId(entity.getSubjectId());
        }
        bean.setUniqueIdentifier(entity.getUniqueIdentifier() != null ? entity.getUniqueIdentifier() : "");
        bean.setDateOfBirth(toDate(entity.getDateOfBirth()));
        String gender = entity.getGender();
        bean.setGender(gender != null && !gender.isEmpty() ? gender.charAt(0) : 'm');
        bean.setDobCollected(entity.getDobCollected() != null && entity.getDobCollected());
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
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

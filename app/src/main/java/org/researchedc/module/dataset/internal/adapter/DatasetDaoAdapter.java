package org.researchedc.module.dataset.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.bean.core.DatasetItemStatus;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.extract.DatasetBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.spi.DatasetDao;
import org.researchedc.module.dataset.entity.DatasetEntity;
import org.researchedc.module.dataset.repository.DatasetRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("datasetDAO")
@Primary
@Transactional(readOnly = true)
public class DatasetDaoAdapter implements DatasetDao {

    private final DatasetRepository datasetRepository;

    public DatasetDaoAdapter(DatasetRepository datasetRepository) {
        this.datasetRepository = datasetRepository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return datasetRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(DatasetBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        DatasetBean bean = (DatasetBean) eb;
        DatasetEntity entity = new DatasetEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(datasetRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        DatasetBean bean = (DatasetBean) eb;
        DatasetEntity entity = datasetRepository.findById(bean.getId())
                .orElseGet(DatasetEntity::new);
        entity.setDatasetId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(datasetRepository.save(entity));
    }

    @Override
    public Collection findAll() {
        return toBeans(datasetRepository.findByStatusId(Status.AVAILABLE.getId()));
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
    public EntityBean findByNameAndStudy(String name, StudyBean study) {
        List<DatasetEntity> entities = datasetRepository.findByNameAndStudyId(name, study.getId());
        return entities.isEmpty() ? new DatasetBean() : toBean(entities.get(0));
    }

    @Override
    public ArrayList findAllByStudyId(int studyId) {
        return toBeans(datasetRepository.findByStudyIdAndStatusId(studyId, Status.AVAILABLE.getId()));
    }

    @Override
    public ArrayList findAllByStudyIdAdmin(int studyId) {
        return toBeans(datasetRepository.findByStudyId(studyId));
    }

    @Override
    public DatasetBean initialDatasetData(int datasetId) {
        return new DatasetBean();
    }

    @Override
    public Collection findAllOrderByStudyIdAndName() {
        return toBeans(datasetRepository.findAllByOrderByStudyIdAscNameAsc());
    }

    @Override
    public Collection findTopFive(StudyBean currentStudy) {
        return toBeans(datasetRepository.findTop5ByStudyIdOrderByDatasetId(currentStudy.getId()));
    }

    @Override
    public Collection findByOwnerId(int ownerId, int studyId) {
        return toBeans(datasetRepository.findByOwnerIdAndStudyId(ownerId, studyId));
    }

    @Override
    @Transactional
    public EntityBean updateAll(EntityBean eb) {
        return update(eb);
    }

    @Override
    @Transactional
    public EntityBean updateGroupMap(DatasetBean db) {
        DatasetEntity entity = datasetRepository.findById(db.getId())
                .orElseGet(DatasetEntity::new);
        entity.setDatasetId(db.getId() > 0 ? db.getId() : null);
        entity.setName(db.getName());
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(datasetRepository.save(entity));
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        DatasetEntity entity = new DatasetEntity();
        entity.setDatasetId((Integer) hm.get("dataset_id"));
        entity.setName((String) hm.get("name"));
        entity.setDescription((String) hm.get("description"));
        entity.setStudyId((Integer) hm.get("study_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setSqlStatement((String) hm.get("sql_statement"));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setApproverId((Integer) hm.get("approver_id"));
        entity.setNumRuns((Integer) hm.get("num_runs"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setDateStart(toLocalDateTime((Date) hm.get("date_start")));
        entity.setDateEnd(toLocalDateTime((Date) hm.get("date_end")));
        entity.setDateLastRun(toLocalDateTime((Date) hm.get("date_last_run")));
        entity.setDatasetItemStatusId((Integer) hm.get("dataset_item_status_id"));
        entity.setOdmMetaDataVersionName((String) hm.get("odm_meta_data_version_name"));
        entity.setOdmMetaDataVersionOid((String) hm.get("odm_meta_data_version_oid"));
        entity.setOdmPriorStudyOid((String) hm.get("odm_prior_study_oid"));
        entity.setOdmPriorMetaDataVersionOid((String) hm.get("odm_prior_meta_data_version_oid"));
        entity.setShowEventLocation((Boolean) hm.get("show_event_location"));
        entity.setShowEventStart((Boolean) hm.get("show_event_start"));
        entity.setShowEventEnd((Boolean) hm.get("show_event_end"));
        entity.setShowSubjectDob((Boolean) hm.get("show_subject_dob"));
        entity.setShowSubjectGender((Boolean) hm.get("show_subject_gender"));
        entity.setShowEventStatus((Boolean) hm.get("show_event_status"));
        entity.setShowSubjectStatus((Boolean) hm.get("show_subject_status"));
        entity.setShowSubjectUniqueId((Boolean) hm.get("show_subject_unique_id"));
        entity.setShowSubjectAgeAtEvent((Boolean) hm.get("show_subject_age_at_event"));
        entity.setShowCrfStatus((Boolean) hm.get("show_crf_status"));
        entity.setShowCrfVersion((Boolean) hm.get("show_crf_version"));
        entity.setShowCrfIntName((Boolean) hm.get("show_crf_int_name"));
        entity.setShowCrfIntDate((Boolean) hm.get("show_crf_int_date"));
        entity.setShowGroupInfo((Boolean) hm.get("show_group_info"));
        entity.setShowDiscInfo((Boolean) hm.get("show_disc_info"));
        entity.setShowSecondaryId((Boolean) hm.get("show_secondary_id"));
        return toBean(entity);
    }

    private void apply(DatasetBean bean, DatasetEntity entity) {
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setStudyId(bean.getStudyId());
        entity.setSqlStatement(bean.getSQLStatement());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setNumRuns(bean.getNumRuns());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
        entity.setApproverId(bean.getApproverId());
        entity.setDateStart(toLocalDateTime(bean.getDateStart()));
        entity.setDateEnd(toLocalDateTime(bean.getDateEnd()));
        entity.setDateLastRun(toLocalDateTime(bean.getDateLastRun()));
        entity.setShowEventLocation(bean.isShowEventLocation());
        entity.setShowEventStart(bean.isShowEventStart());
        entity.setShowEventEnd(bean.isShowEventEnd());
        entity.setShowSubjectDob(bean.isShowSubjectDob());
        entity.setShowSubjectGender(bean.isShowSubjectGender());
        entity.setShowEventStatus(bean.isShowEventStatus());
        entity.setShowSubjectStatus(bean.isShowSubjectStatus());
        entity.setShowSubjectUniqueId(bean.isShowSubjectUniqueIdentifier());
        entity.setShowSubjectAgeAtEvent(bean.isShowSubjectAgeAtEvent());
        entity.setShowCrfStatus(bean.isShowCRFstatus());
        entity.setShowCrfVersion(bean.isShowCRFversion());
        entity.setShowCrfIntName(bean.isShowCRFinterviewerName());
        entity.setShowCrfIntDate(bean.isShowCRFinterviewerDate());
        entity.setShowGroupInfo(bean.isShowSubjectGroupInformation());
        entity.setShowSecondaryId(bean.isShowSubjectSecondaryId());
        entity.setOdmMetaDataVersionName(bean.getODMMetaDataVersionName());
        entity.setOdmMetaDataVersionOid(bean.getODMMetaDataVersionOid());
        entity.setOdmPriorStudyOid(bean.getODMPriorStudyOid());
        entity.setOdmPriorMetaDataVersionOid(bean.getODMPriorMetaDataVersionOid());
        if (bean.getDatasetItemStatus() != null) {
            entity.setDatasetItemStatusId(bean.getDatasetItemStatus().getId());
        }
    }

    private ArrayList toBeans(List<DatasetEntity> entities) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .sorted(Comparator.comparing(DatasetEntity::getDatasetId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private DatasetBean toBean(DatasetEntity entity) {
        DatasetBean bean = new DatasetBean();
        if (entity.getDatasetId() != null) {
            bean.setId(entity.getDatasetId());
        }
        bean.setName(entity.getName() != null ? entity.getName() : "");
        bean.setDescription(entity.getDescription());
        bean.setStudyId(valueOrZero(entity.getStudyId()));
        bean.setSQLStatement(entity.getSqlStatement());
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setNumRuns(valueOrZero(entity.getNumRuns()));
        bean.setDateStart(toDate(entity.getDateStart()));
        bean.setDateEnd(toDate(entity.getDateEnd()));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setDateLastRun(toDate(entity.getDateLastRun()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setApproverId(valueOrZero(entity.getApproverId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setShowEventLocation(valueOrFalse(entity.getShowEventLocation()));
        bean.setShowEventStart(valueOrFalse(entity.getShowEventStart()));
        bean.setShowEventEnd(valueOrFalse(entity.getShowEventEnd()));
        bean.setShowSubjectDob(valueOrFalse(entity.getShowSubjectDob()));
        bean.setShowSubjectGender(valueOrFalse(entity.getShowSubjectGender()));
        bean.setShowEventStatus(valueOrFalse(entity.getShowEventStatus()));
        bean.setShowSubjectStatus(valueOrFalse(entity.getShowSubjectStatus()));
        bean.setShowSubjectUniqueIdentifier(valueOrFalse(entity.getShowSubjectUniqueId()));
        bean.setShowSubjectAgeAtEvent(valueOrFalse(entity.getShowSubjectAgeAtEvent()));
        bean.setShowCRFstatus(valueOrFalse(entity.getShowCrfStatus()));
        bean.setShowCRFversion(valueOrFalse(entity.getShowCrfVersion()));
        bean.setShowCRFinterviewerName(valueOrFalse(entity.getShowCrfIntName()));
        bean.setShowCRFinterviewerDate(valueOrFalse(entity.getShowCrfIntDate()));
        bean.setShowSubjectGroupInformation(valueOrFalse(entity.getShowGroupInfo()));
        bean.setShowSubjectSecondaryId(valueOrFalse(entity.getShowSecondaryId()));
        bean.setODMMetaDataVersionName(entity.getOdmMetaDataVersionName());
        bean.setODMMetaDataVersionOid(entity.getOdmMetaDataVersionOid());
        bean.setODMPriorStudyOid(entity.getOdmPriorStudyOid());
        bean.setODMPriorMetaDataVersionOid(entity.getOdmPriorMetaDataVersionOid());
        if (entity.getDatasetItemStatusId() != null) {
            bean.setDatasetItemStatus(DatasetItemStatus.get(entity.getDatasetItemStatusId()));
        }
        return bean;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private boolean valueOrFalse(Boolean value) {
        return value != null && value;
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

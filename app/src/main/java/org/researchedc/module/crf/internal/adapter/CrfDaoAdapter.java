package org.researchedc.module.crf.internal.adapter;

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
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.dao.spi.ICrfDAO;
import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.repository.CrfRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.module.crf.repository.ItemRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("CRFDAO")
@Primary
@Transactional(readOnly = true)
public class CrfDaoAdapter implements ICrfDAO {

    private final CrfRepository crfRepository;
    private final CrfVersionRepository crfVersionRepository;
    private final ItemRepository itemRepository;

    public CrfDaoAdapter(CrfRepository crfRepository,
                         CrfVersionRepository crfVersionRepository,
                         ItemRepository itemRepository) {
        this.crfRepository = crfRepository;
        this.crfVersionRepository = crfVersionRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return crfRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(CRFBean::new);
    }

    @Override
    public EntityBean findByName(String name) {
        return crfRepository.findByName(name)
                .map(this::toBean)
                .orElseGet(CRFBean::new);
    }

    @Override
    public EntityBean findAnotherByName(String name, int crfId) {
        return crfRepository.findAnotherByName(name, crfId)
                .map(this::toBean)
                .orElseGet(CRFBean::new);
    }

    @Override
    public CRFBean findByVersionId(int crfVersionId) {
        return crfVersionRepository.findById(crfVersionId)
                .map(CrfVersionEntity::getCrfId)
                .flatMap(crfRepository::findById)
                .map(this::toBean)
                .orElseGet(CRFBean::new);
    }

    @Override
    public CRFBean findByOid(String oid) {
        return crfRepository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public CRFBean findByItemOid(String itemOid) {
        return crfRepository.findByItemOid(itemOid)
                .map(this::toBean)
                .orElseGet(CRFBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        CRFBean bean = (CRFBean) eb;
        CrfEntity entity = new CrfEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(crfRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        CRFBean bean = (CRFBean) eb;
        CrfEntity entity = crfRepository.findById(bean.getId()).orElseGet(CrfEntity::new);
        entity.setCrfId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(crfRepository.save(entity));
    }

    @Override
    public Collection findAll() {
        return toBeans(crfRepository.findAll());
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
    public Collection findAllByStudy(int studyId) {
        return toBeans(crfRepository.findBySourceStudyId(studyId));
    }

    @Override
    public Collection findAllByStatus(Status status) {
        return toBeans(crfRepository.findByStatusId(status.getId()));
    }

    @Override
    public Collection findAllActiveByDefinition(StudyEventDefinitionBean definition) {
        return toBeans(crfRepository.findBySourceStudyIdAndStatusId(
                definition.getStudyId(), Status.AVAILABLE.getId()));
    }

    @Override
    public Collection findAllActiveByDefinitions(int studyId) {
        return toBeans(crfRepository.findBySourceStudyIdAndStatusId(
                studyId, Status.AVAILABLE.getId()));
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType,
                                          String strOrderByColumn, boolean blnAscendingSort,
                                          String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    @Override
    public ArrayList<CRFBean> findAllByOid(String oid) {
        return crfRepository.findByOcOid(oid)
                .map(e -> {
                    ArrayList<CRFBean> list = new ArrayList<>();
                    list.add(toBean(e));
                    return list;
                })
                .orElseGet(ArrayList::new);
    }

    @Override
    public Integer getCountofActiveCRFs() {
        return 0;
    }

    @Override
    public String getValidOid(CRFBean crfBean, String crfName) {
        return "";
    }

    @Override
    public Map<Integer, CRFBean> buildCrfById(Integer studySubjectId) {
        return new HashMap<>();
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        CRFBean bean = new CRFBean();
        bean.setId(valueOrZero((Integer) hm.get("crf_id")));
        bean.setName((String) hm.get("name"));
        bean.setDescription((String) hm.get("description"));
        bean.setOid((String) hm.get("oc_oid"));
        bean.setStudyId(valueOrZero((Integer) hm.get("source_study_id")));
        bean.setOwnerId(valueOrZero((Integer) hm.get("owner_id")));
        bean.setUpdaterId(valueOrZero((Integer) hm.get("update_id")));
        bean.setCreatedDate((Date) hm.get("date_created"));
        bean.setUpdatedDate((Date) hm.get("date_updated"));
        bean.setStatus(Status.getFromMap(valueOrZero((Integer) hm.get("status_id"))));
        return bean;
    }

    private void apply(CRFBean bean, CrfEntity entity) {
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setOcOid(bean.getOid());
        entity.setSourceStudyId(bean.getStudyId() > 0 ? bean.getStudyId() : null);
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
    }

    private ArrayList<CRFBean> toBeans(List<CrfEntity> entities) {
        ArrayList<CRFBean> beans = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(CrfEntity::getCrfId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private CRFBean toBean(CrfEntity entity) {
        CRFBean bean = new CRFBean();
        if (entity.getCrfId() != null) {
            bean.setId(entity.getCrfId());
        }
        bean.setName(entity.getName());
        bean.setDescription(entity.getDescription());
        bean.setOid(entity.getOcOid());
        bean.setStudyId(valueOrZero(entity.getSourceStudyId()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
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

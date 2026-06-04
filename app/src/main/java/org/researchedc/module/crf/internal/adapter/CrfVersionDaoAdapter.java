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

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.spi.ICrfVersionDAO;
import org.researchedc.domain.datamap.CrfVersion;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.repository.CrfRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("CRFVersionDAO")
@Primary
@Transactional(readOnly = true)
public class CrfVersionDaoAdapter implements ICrfVersionDAO {

    private final CrfVersionRepository crfVersionRepository;
    private final CrfRepository crfRepository;

    public CrfVersionDaoAdapter(CrfVersionRepository crfVersionRepository,
                                CrfRepository crfRepository) {
        this.crfVersionRepository = crfVersionRepository;
        this.crfRepository = crfRepository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return crfVersionRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(CRFVersionBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        CRFVersionBean bean = (CRFVersionBean) eb;
        CrfVersionEntity entity = new CrfVersionEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(crfVersionRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        CRFVersionBean bean = (CRFVersionBean) eb;
        CrfVersionEntity entity = crfVersionRepository.findById(bean.getId())
                .orElseGet(CrfVersionEntity::new);
        entity.setCrfVersionId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(crfVersionRepository.save(entity));
    }

    @Override
    public void setTypesExpected() {
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        CRFVersionBean bean = new CRFVersionBean();
        bean.setId(valueOrZero((Integer) hm.get("crf_version_id")));
        bean.setCrfId(valueOrZero((Integer) hm.get("crf_id")));
        bean.setName((String) hm.get("name"));
        bean.setDescription((String) hm.get("description"));
        bean.setRevisionNotes((String) hm.get("revision_notes"));
        bean.setOid((String) hm.get("oc_oid"));
        bean.setOwnerId(valueOrZero((Integer) hm.get("owner_id")));
        bean.setUpdaterId(valueOrZero((Integer) hm.get("update_id")));
        bean.setCreatedDate((Date) hm.get("date_created"));
        bean.setUpdatedDate((Date) hm.get("date_updated"));
        bean.setStatus(Status.getFromMap(valueOrZero((Integer) hm.get("status_id"))));
        return bean;
    }

    @Override
    public Collection findAll() {
        return toBeans(crfVersionRepository.findAll());
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByCRF(int crfId) {
        return toBeans(crfVersionRepository.findByCrfId(crfId));
    }

    @Override
    public Collection findAllActiveByCRF(int crfId) {
        return toBeans(crfVersionRepository.findByCrfIdAndStatusId(crfId, Status.AVAILABLE.getId()));
    }

    @Override
    public Collection findItemFromMap(int versionId) {
        return new ArrayList();
    }

    @Override
    public Collection findItemUsedByOtherVersion(int versionId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findNotSharedItemsByVersion(int versionId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findDefCRFVersionsByStudyEvent(int studyEventDefinitionId) {
        return new ArrayList();
    }

    @Override
    public boolean isItemUsedByOtherVersion(int versionId) {
        return false;
    }

    @Override
    public boolean hasItemData(int itemId) {
        return false;
    }

    @Override
    public EntityBean findByFullName(String version, String crfName) {
        return crfRepository.findByName(crfName)
                .flatMap(crf -> crfVersionRepository.findByNameAndCrfId(version, crf.getCrfId()))
                .map(this::toBean)
                .orElseGet(CRFVersionBean::new);
    }

    @Override
    @Transactional
    public void delete(int id) {
    }

    @Override
    public ArrayList generateDeleteQueries(int versionId, ArrayList items) {
        return new ArrayList();
    }

    @Override
    public String getValidOid(CRFVersionBean crfVersion, String crfName, String crfVersionName) {
        return "";
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
    public ArrayList findAllByOid(String oid) {
        return toBeans(crfVersionRepository.findByOcOidContaining(oid));
    }

    @Override
    public int getCRFIdFromCRFVersionId(int CRFVersionId) {
        return crfVersionRepository.findById(CRFVersionId)
                .map(CrfVersionEntity::getCrfId)
                .map(this::valueOrZero)
                .orElse(0);
    }

    @Override
    public ArrayList findAllByCRFId(int CRFId) {
        return toBeans(crfVersionRepository.findByCrfId(CRFId));
    }

    @Override
    public Integer findCRFVersionId(int crfId, String versionName) {
        return crfVersionRepository.findByNameAndCrfId(versionName, crfId)
                .map(CrfVersionEntity::getCrfVersionId)
                .orElse(null);
    }

    @Override
    public CRFVersionBean findByOid(String oid) {
        return crfVersionRepository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public Map<Integer, CRFVersionBean> buildCrfVersionById(Integer studySubjectId) {
        return new HashMap<>();
    }

    @Override
    public CrfVersion findByOcOID(String OCOID) {
        return null;
    }

    private void apply(CRFVersionBean bean, CrfVersionEntity entity) {
        entity.setCrfId(bean.getCrfId() > 0 ? bean.getCrfId() : null);
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setRevisionNotes(bean.getRevisionNotes());
        entity.setOcOid(bean.getOid());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
        entity.setXform(bean.getXform());
        entity.setXformName(bean.getXformName());
    }

    private ArrayList<CRFVersionBean> toBeans(List<CrfVersionEntity> entities) {
        ArrayList<CRFVersionBean> beans = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(CrfVersionEntity::getCrfVersionId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private CRFVersionBean toBean(CrfVersionEntity entity) {
        CRFVersionBean bean = new CRFVersionBean();
        if (entity.getCrfVersionId() != null) {
            bean.setId(entity.getCrfVersionId());
        }
        bean.setCrfId(valueOrZero(entity.getCrfId()));
        bean.setName(entity.getName() != null ? entity.getName() : "");
        bean.setDescription(entity.getDescription() != null ? entity.getDescription() : "");
        bean.setRevisionNotes(entity.getRevisionNotes() != null ? entity.getRevisionNotes() : "");
        bean.setOid(entity.getOcOid());
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setXform(entity.getXform());
        bean.setXformName(entity.getXformName());
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

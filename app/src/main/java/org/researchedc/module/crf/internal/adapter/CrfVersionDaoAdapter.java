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

import org.researchedc.app.dto.CrfVersionDTO;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.repository.CrfRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.app.dto.Status;
import org.researchedc.module.dataimport.service.ImportCrfVersionPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("CRFVersionDAO")
@Primary
@Transactional(readOnly = true)
public class CrfVersionDaoAdapter implements ImportCrfVersionPort {

    private final CrfVersionRepository crfVersionRepository;
    private final CrfRepository crfRepository;

    public CrfVersionDaoAdapter(CrfVersionRepository crfVersionRepository,
                                CrfRepository crfRepository) {
        this.crfVersionRepository = crfVersionRepository;
        this.crfRepository = crfRepository;
    }

    public CrfVersionDTO findByPK(int ID) {
        return crfVersionRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(CrfVersionDTO::new);
    }

    @Transactional
    public CrfVersionDTO create(CrfVersionDTO dto) {
        CrfVersionEntity entity = new CrfVersionEntity();
        apply(dto, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(crfVersionRepository.save(entity));
    }

    @Transactional
    public CrfVersionDTO update(CrfVersionDTO dto) {
        CrfVersionEntity entity = crfVersionRepository.findById(dto.getId())
                .orElseGet(CrfVersionEntity::new);
        entity.setCrfVersionId(dto.getId() > 0 ? dto.getId() : null);
        apply(dto, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(crfVersionRepository.save(entity));
    }

    public void setTypesExpected() {
    }

    public Object getEntityFromHashMap(HashMap hm) {
        CrfVersionDTO dto = new CrfVersionDTO();
        dto.setId(valueOrZero((Integer) hm.get("crf_version_id")));
        dto.setCrfId(valueOrZero((Integer) hm.get("crf_id")));
        dto.setName((String) hm.get("name"));
        dto.setDescription((String) hm.get("description"));
        dto.setRevisionNotes((String) hm.get("revision_notes"));
        dto.setOid((String) hm.get("oc_oid"));
        dto.setOwnerId(valueOrZero((Integer) hm.get("owner_id")));
        dto.setUpdaterId(valueOrZero((Integer) hm.get("update_id")));
        dto.setCreatedDate((Date) hm.get("date_created"));
        dto.setUpdatedDate((Date) hm.get("date_updated"));
        dto.setStatusName(Status.getFromMap(valueOrZero((Integer) hm.get("status_id"))).getName());
        return dto;
    }

    public Collection findAll() {
        return toBeans(crfVersionRepository.findAll());
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    public Collection findAllByCRF(int crfId) {
        return toBeans(crfVersionRepository.findByCrfId(crfId));
    }

    public Collection findAllActiveByCRF(int crfId) {
        return toBeans(crfVersionRepository.findByCrfIdAndStatusId(crfId, Status.AVAILABLE.getId()));
    }

    public Collection findItemFromMap(int versionId) {
        return new ArrayList();
    }

    public Collection findItemUsedByOtherVersion(int versionId) {
        return new ArrayList();
    }

    public ArrayList findNotSharedItemsByVersion(int versionId) {
        return new ArrayList();
    }

    public ArrayList findDefCRFVersionsByStudyEvent(int studyEventDefinitionId) {
        return new ArrayList();
    }

    public boolean isItemUsedByOtherVersion(int versionId) {
        return false;
    }

    public boolean hasItemData(int itemId) {
        return false;
    }

    public CrfVersionDTO findByFullName(String version, String crfName) {
        return crfRepository.findByName(crfName)
                .flatMap(crf -> crfVersionRepository.findByNameAndCrfId(version, crf.getCrfId()))
                .map(this::toBean)
                .orElseGet(CrfVersionDTO::new);
    }

    @Transactional
    public void delete(int id) {
    }

    public ArrayList generateDeleteQueries(int versionId, ArrayList items) {
        return new ArrayList();
    }

    public String getValidOid(CrfVersionDTO crfVersion, String crfName, String crfVersionName) {
        return "";
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType,
                                          String strOrderByColumn, boolean blnAscendingSort,
                                          String strSearchPhrase) {
        return new ArrayList();
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    public ArrayList findAllByOid(String oid) {
        return toBeans(crfVersionRepository.findByOcOidContaining(oid));
    }

    @Override
    public List<ImportCrfVersion> findAllImportCrfVersionsByOid(String oid) {
        return crfVersionRepository.findByOcOidContaining(oid).stream()
                .map(entity -> new ImportCrfVersion(entity.getCrfVersionId()))
                .toList();
    }

    public int getCRFIdFromCRFVersionId(int CRFVersionId) {
        return crfVersionRepository.findById(CRFVersionId)
                .map(CrfVersionEntity::getCrfId)
                .map(this::valueOrZero)
                .orElse(0);
    }

    public ArrayList findAllByCRFId(int CRFId) {
        return toBeans(crfVersionRepository.findByCrfId(CRFId));
    }

    public Integer findCRFVersionId(int crfId, String versionName) {
        return crfVersionRepository.findByNameAndCrfId(versionName, crfId)
                .map(CrfVersionEntity::getCrfVersionId)
                .orElse(null);
    }

    public CrfVersionDTO findByOid(String oid) {
        return crfVersionRepository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    public Map<Integer, CrfVersionDTO> buildCrfVersionById(Integer studySubjectId) {
        return new HashMap<>();
    }

    private void apply(CrfVersionDTO dto, CrfVersionEntity entity) {
        entity.setCrfId(dto.getCrfId() > 0 ? dto.getCrfId() : null);
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setRevisionNotes(dto.getRevisionNotes());
        entity.setOcOid(dto.getOid());
        entity.setStatusId(dto.getStatus() != null ? dto.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(dto.getOwnerId());
        entity.setUpdateId(dto.getUpdaterId());
        entity.setXform(dto.getXform());
        entity.setXformName(dto.getXformName());
    }

    private ArrayList<CrfVersionDTO> toBeans(List<CrfVersionEntity> entities) {
        ArrayList<CrfVersionDTO> dtos = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(CrfVersionEntity::getCrfVersionId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    private CrfVersionDTO toBean(CrfVersionEntity entity) {
        CrfVersionDTO dto = new CrfVersionDTO();
        if (entity.getCrfVersionId() != null) {
            dto.setId(entity.getCrfVersionId());
        }
        dto.setCrfId(valueOrZero(entity.getCrfId()));
        dto.setName(entity.getName() != null ? entity.getName() : "");
        dto.setDescription(entity.getDescription() != null ? entity.getDescription() : "");
        dto.setRevisionNotes(entity.getRevisionNotes() != null ? entity.getRevisionNotes() : "");
        dto.setOid(entity.getOcOid());
        dto.setOwnerId(valueOrZero(entity.getOwnerId()));
        dto.setUpdaterId(valueOrZero(entity.getUpdateId()));
        dto.setCreatedDate(toDate(entity.getDateCreated()));
        dto.setUpdatedDate(toDate(entity.getDateUpdated()));
        dto.setStatusName(Status.getFromMap(valueOrZero(entity.getStatusId())).getName());
        dto.setXform(entity.getXform());
        dto.setXformName(entity.getXformName());
        return dto;
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

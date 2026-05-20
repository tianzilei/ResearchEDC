package org.researchedc.module.legacy.controller;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.dao.admin.CRFDAO;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.submit.CRFVersionDAO;
import org.researchedc.module.legacy.dto.CreateCrfRequest;
import org.researchedc.module.legacy.dto.CrfManageDTO;
import org.researchedc.module.legacy.dto.CrfVersionManageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/crfs")
public class LegacyCrfManageController {

    private final CRFDAO crfDao;
    private final CRFVersionDAO crfVersionDao;
    private final UserAccountDAO userAccountDao;

    public LegacyCrfManageController(CRFDAO crfDao, CRFVersionDAO crfVersionDao,
                                     UserAccountDAO userAccountDao) {
        this.crfDao = crfDao;
        this.crfVersionDao = crfVersionDao;
        this.userAccountDao = userAccountDao;
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<CrfManageDTO>> listCrfs() {
        List<CrfManageDTO> result = new ArrayList<>();
        for (Object obj : crfDao.findAll()) {
            result.add(toDto((CRFBean) obj));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrfManageDTO> getCrf(@PathVariable int id) {
        CRFBean bean = (CRFBean) crfDao.findByPK(id);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(bean));
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<CrfManageDTO> createCrf(@RequestBody CreateCrfRequest request) {
        CRFBean bean = new CRFBean();
        bean.setName(request.getName());
        bean.setDescription(request.getDescription());
        bean.setStatusId(1);
        UserAccountBean defaultUser = (UserAccountBean) userAccountDao.findByPK(1);
        bean.setOwner(defaultUser);
        bean = (CRFBean) crfDao.create(bean);
        return ResponseEntity.ok(toDto(bean));
    }

    @PutMapping("/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<CrfManageDTO> updateCrf(@PathVariable int id, @RequestBody CreateCrfRequest request) {
        CRFBean bean = (CRFBean) crfDao.findByPK(id);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        bean.setName(request.getName());
        bean.setDescription(request.getDescription());
        crfDao.update(bean);
        return ResponseEntity.ok(toDto(bean));
    }

    @GetMapping("/{id}/versions")
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<CrfVersionManageDTO>> listVersions(@PathVariable int id) {
        List<CrfVersionManageDTO> result = new ArrayList<>();
        for (Object obj : crfVersionDao.findAllByCRF(id)) {
            result.add(toVersionDto((CRFVersionBean) obj));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/versions/{versionId}")
    public ResponseEntity<CrfVersionManageDTO> getVersion(@PathVariable int versionId) {
        CRFVersionBean bean = (CRFVersionBean) crfVersionDao.findByPK(versionId);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toVersionDto(bean));
    }

    @PostMapping("/{crfId}/versions")
    @SuppressWarnings("unchecked")
    public ResponseEntity<CrfVersionManageDTO> createVersion(@PathVariable int crfId,
            @RequestBody CrfVersionManageDTO request) {
        CRFVersionBean bean = new CRFVersionBean();
        bean.setName(request.getName());
        bean.setDescription(request.getDescription() != null ? request.getDescription() : "");
        bean.setRevisionNotes(request.getRevisionNotes() != null ? request.getRevisionNotes() : "");
        bean.setCrfId(crfId);
        bean.setStatusId(1);
        UserAccountBean defaultUser = (UserAccountBean) userAccountDao.findByPK(1);
        bean.setOwner(defaultUser);
        bean = (CRFVersionBean) crfVersionDao.create(bean);
        return ResponseEntity.ok(toVersionDto(bean));
    }

    @DeleteMapping("/versions/{versionId}")
    public ResponseEntity<Void> deleteVersion(@PathVariable int versionId) {
        CRFVersionBean bean = (CRFVersionBean) crfVersionDao.findByPK(versionId);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        crfVersionDao.delete(versionId);
        return ResponseEntity.noContent().build();
    }

    private CrfManageDTO toDto(CRFBean bean) {
        CrfManageDTO dto = new CrfManageDTO();
        dto.setCrfId(bean.getId());
        dto.setName(bean.getName());
        dto.setDescription(bean.getDescription());
        dto.setOcOid(bean.getOid());
        dto.setStatus(bean.getStatus() != null ? bean.getStatus().getName() : null);
        dto.setDateCreated(bean.getCreatedDate());
        return dto;
    }

    private CrfVersionManageDTO toVersionDto(CRFVersionBean bean) {
        CrfVersionManageDTO dto = new CrfVersionManageDTO();
        dto.setCrfVersionId(bean.getId());
        dto.setCrfId(bean.getCrfId());
        dto.setName(bean.getName());
        dto.setDescription(bean.getDescription());
        dto.setRevisionNotes(bean.getRevisionNotes());
        if (bean.getStatus() != null) {
            dto.setStatus(bean.getStatus().getName());
        }
        dto.setDateCreated(bean.getCreatedDate());
        return dto;
    }
}

package org.researchedc.module.legacy.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;

import org.researchedc.bean.login.UserAccountBean;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.login.UserAccountDAO;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.module.legacy.dto.CreateDiscrepancyNoteRequest;
import org.researchedc.module.legacy.dto.DiscrepancyNoteDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/discrepancy-notes")
public class LegacyDiscrepancyNoteController {

    private final DiscrepancyNoteDAO discrepancyNoteDao;
    private final StudyDAO studyDao;
    private final UserAccountDAO userAccountDao;

    public LegacyDiscrepancyNoteController(DataSource dataSource) {
        this.discrepancyNoteDao = new DiscrepancyNoteDAO(dataSource);
        this.studyDao = new StudyDAO(dataSource);
        this.userAccountDao = new UserAccountDAO(dataSource);
    }

    @GetMapping
    public ResponseEntity<List<DiscrepancyNoteDTO>> listNotes(
            @RequestParam(required = false) Integer eventCrfId,
            @RequestParam(required = false) Integer studyId) {
        if (eventCrfId != null) {
            return listNotesByEventCrf(eventCrfId);
        }
        if (studyId != null) {
            return listNotesByStudy(studyId);
        }
        return ResponseEntity.ok(List.of());
    }

    private ResponseEntity<List<DiscrepancyNoteDTO>> listNotesByEventCrf(int eventCrfId) {
        List<DiscrepancyNoteDTO> result = new ArrayList<>();
        for (DiscrepancyNoteBean bean : discrepancyNoteDao.findAllParentItemNotesByEventCRF(eventCrfId)) {
            if (bean.getParentDnId() == 0) {
                result.add(toDto(bean));
            }
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<List<DiscrepancyNoteDTO>> listNotesByStudy(int studyId) {
        List<DiscrepancyNoteDTO> result = new ArrayList<>();
        StudyBean study = (StudyBean) studyDao.findByPK(studyId);
        if (study == null || study.getId() == 0) {
            return ResponseEntity.ok(List.of());
        }
        for (Object obj : discrepancyNoteDao.findAllParentsByStudy(study)) {
            result.add(toDto((DiscrepancyNoteBean) obj));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscrepancyNoteDTO> getNote(@PathVariable int id) {
        Object obj = discrepancyNoteDao.findByPK(id);
        if (!(obj instanceof DiscrepancyNoteBean)) {
            return ResponseEntity.notFound().build();
        }
        DiscrepancyNoteBean bean = (DiscrepancyNoteBean) obj;
        if (bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(bean));
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<DiscrepancyNoteDTO> createNote(
            @RequestBody CreateDiscrepancyNoteRequest request) {
        DiscrepancyNoteBean parent = new DiscrepancyNoteBean();
        parent.setDescription(request.getDescription());
        parent.setDetailedNotes(request.getDetailedNotes());
        parent.setEntityType(request.getEntityType());
        parent.setEntityId(request.getEntityId());
        parent.setStudyId(request.getStudyId());
        parent.setDiscrepancyNoteTypeId(1);
        parent.setResolutionStatusId(1);
        parent.setColumn("value");
        parent.setEventCRFId(request.getEventCrfId());

        UserAccountBean defaultUser = (UserAccountBean) userAccountDao.findByPK(1);
        parent.setOwner(defaultUser);

        parent = (DiscrepancyNoteBean) discrepancyNoteDao.create(parent);
        discrepancyNoteDao.createMapping(parent);

        return ResponseEntity.ok(toDto(parent));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<DiscrepancyNoteDTO> resolveNote(@PathVariable int id) {
        Object obj = discrepancyNoteDao.findByPK(id);
        if (!(obj instanceof DiscrepancyNoteBean)) {
            return ResponseEntity.notFound().build();
        }
        DiscrepancyNoteBean bean = (DiscrepancyNoteBean) obj;
        if (bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        bean.setResolutionStatusId(2);
        discrepancyNoteDao.update(bean);
        return ResponseEntity.ok(toDto(bean));
    }

    private DiscrepancyNoteDTO toDto(DiscrepancyNoteBean bean) {
        DiscrepancyNoteDTO dto = new DiscrepancyNoteDTO();
        dto.setDiscrepancyNoteId(bean.getId());
        dto.setDescription(bean.getDescription());
        dto.setDetailedNotes(bean.getDetailedNotes());
        dto.setType(bean.getDisType() != null ? bean.getDisType().getName() : null);
        dto.setResolutionStatus(bean.getResStatus() != null ? bean.getResStatus().getName() : null);
        dto.setEntityType(bean.getEntityType());
        dto.setColumn(bean.getColumn());
        dto.setEntityId(bean.getEntityId());
        dto.setStudyId(bean.getStudyId());
        dto.setOwnerId(bean.getOwnerId());
        dto.setDateCreated(bean.getCreatedDate());
        dto.setParentDnId(bean.getParentDnId());
        dto.setHasChildren(bean.getNumChildren() > 0);
        dto.setEventCRFId(bean.getEventCRFId());
        dto.setSubjectName(bean.getSubjectName());
        dto.setEventName(bean.getEventName());
        dto.setCrfName(bean.getCrfName());
        dto.setEntityName(bean.getEntityName());
        return dto;
    }
}

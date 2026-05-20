package org.researchedc.module.legacy.controller;

import java.util.ArrayList;
import java.util.List;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.module.legacy.dto.StudySummaryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/studies")
public class LegacyStudyController {

    private final StudyDAO studyDao;

    public LegacyStudyController(StudyDAO studyDao) {
        this.studyDao = studyDao;
    }

    @GetMapping
    public ResponseEntity<List<StudySummaryDTO>> listStudies() {
        List<StudySummaryDTO> result = new ArrayList<>();
        for (Object obj : studyDao.findAll()) {
            StudyBean bean = (StudyBean) obj;
            result.add(toDto(bean));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudySummaryDTO> getStudy(@PathVariable int id) {
        StudyBean bean = (StudyBean) studyDao.findByPK(id);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(bean));
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudySummaryDTO>> searchStudies(
            @RequestParam String query) {
        List<StudySummaryDTO> result = new ArrayList<>();
        for (Object obj : studyDao.findAll()) {
            StudyBean bean = (StudyBean) obj;
            if (bean.getName() != null
                    && bean.getName().toLowerCase().contains(query.toLowerCase())) {
                result.add(toDto(bean));
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/sites")
    public ResponseEntity<List<StudySummaryDTO>> listSites(@PathVariable int id) {
        List<StudySummaryDTO> result = new ArrayList<>();
        for (Object obj : studyDao.findAllByParent(id)) {
            result.add(toDto((StudyBean) obj));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-oid")
    public ResponseEntity<StudySummaryDTO> findByOid(@RequestParam String oid) {
        StudyBean bean = (StudyBean) studyDao.findByOid(oid);
        if (bean == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(bean));
    }

    private static StudySummaryDTO toDto(StudyBean bean) {
        StudySummaryDTO dto = new StudySummaryDTO();
        dto.setStudyId(bean.getId());
        dto.setName(bean.getName());
        dto.setIdentifier(bean.getIdentifier());
        dto.setOid(bean.getOid());
        dto.setType(bean.getType() != null ? bean.getType().getName() : null);
        dto.setStatus(bean.getStatus() != null ? bean.getStatus().getName() : null);
        dto.setPrincipalInvestigator(bean.getPrincipalInvestigator());
        dto.setDateCreated(bean.getCreatedDate());
        return dto;
    }
}

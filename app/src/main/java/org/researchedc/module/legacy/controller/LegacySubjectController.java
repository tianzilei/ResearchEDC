package org.researchedc.module.legacy.controller;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.researchedc.bean.managestudy.StudySubjectBean;
import org.researchedc.dao.managestudy.StudySubjectDAO;
import org.researchedc.module.legacy.dto.SubjectDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/subjects")
public class LegacySubjectController {

    private final StudySubjectDAO studySubjectDao;

    @SuppressWarnings("unchecked")
    public LegacySubjectController(DataSource dataSource) {
        this.studySubjectDao = new StudySubjectDAO(dataSource);
    }

    @GetMapping
    public ResponseEntity<List<SubjectDTO>> listSubjects(@RequestParam int studyId) {
        List<SubjectDTO> result = new ArrayList<>();
        for (Object obj : studySubjectDao.findAllByStudyId(studyId)) {
            StudySubjectBean bean = (StudySubjectBean) obj;
            result.add(toDto(bean));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> getSubject(@PathVariable int id) {
        StudySubjectBean bean = (StudySubjectBean) studySubjectDao.findByPK(id);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(bean));
    }

    @GetMapping("/by-label")
    public ResponseEntity<List<SubjectDTO>> searchByLabel(
            @RequestParam int studyId, @RequestParam String label) {
        List<SubjectDTO> result = new ArrayList<>();
        for (Object obj : studySubjectDao.findAllByStudyId(studyId)) {
            StudySubjectBean bean = (StudySubjectBean) obj;
            if (bean.getLabel() != null
                    && bean.getLabel().toLowerCase().contains(label.toLowerCase())) {
                result.add(toDto(bean));
            }
        }
        return ResponseEntity.ok(result);
    }

    private static SubjectDTO toDto(StudySubjectBean bean) {
        SubjectDTO dto = new SubjectDTO();
        dto.setStudySubjectId(bean.getId());
        dto.setStudyId(bean.getStudyId());
        dto.setLabel(bean.getLabel());
        dto.setSecondaryLabel(bean.getSecondaryLabel());
        dto.setUniqueIdentifier(bean.getUniqueIdentifier());
        dto.setEnrollmentDate(bean.getEnrollmentDate());
        dto.setStatus(bean.getStatus() != null ? bean.getStatus().getName() : null);
        return dto;
    }
}

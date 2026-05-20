package org.researchedc.module.legacy.controller;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.bean.extract.DatasetBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.extract.DatasetDAO;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.module.legacy.dto.DatasetDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/datasets")
public class LegacyDatasetController {

    private final DatasetDAO datasetDao;
    private final StudyDAO studyDao;

    public LegacyDatasetController(DatasetDAO datasetDao, StudyDAO studyDao) {
        this.datasetDao = datasetDao;
        this.studyDao = studyDao;
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<DatasetDTO>> listDatasets(
            @RequestParam(required = false) Integer studyId) {
        List<DatasetDTO> result = new ArrayList<>();
        if (studyId != null) {
            for (Object obj : datasetDao.findAllByStudyId(studyId)) {
                result.add(toDto((DatasetBean) obj));
            }
        } else {
            for (Object obj : datasetDao.findAll()) {
                result.add(toDto((DatasetBean) obj));
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatasetDTO> getDataset(@PathVariable int id) {
        DatasetBean bean = (DatasetBean) datasetDao.findByPK(id);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(bean));
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<DatasetDTO> createDataset(@RequestParam String name,
            @RequestParam int studyId) {
        DatasetBean bean = new DatasetBean();
        bean.setName(name);
        StudyBean study = (StudyBean) studyDao.findByPK(studyId);
        if (study != null) {
            bean.setStudyId(study.getId());
        }
        bean = (DatasetBean) datasetDao.create(bean);
        return ResponseEntity.ok(toDto(bean));
    }

    private DatasetDTO toDto(DatasetBean bean) {
        DatasetDTO dto = new DatasetDTO();
        dto.setDatasetId(bean.getId());
        dto.setName(bean.getName());
        dto.setDescription(bean.getDescription());
        dto.setStudyId(bean.getStudyId());
        dto.setOwnerId(bean.getOwnerId());
        dto.setDateCreated(bean.getCreatedDate());
        if (bean.getStatus() != null) {
            dto.setStatus(bean.getStatus().getName());
        }
        return dto;
    }
}

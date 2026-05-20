package org.researchedc.module.legacy.controller;

import java.util.ArrayList;
import java.util.List;

import org.researchedc.bean.extract.FilterBean;
import org.researchedc.dao.extract.FilterDAO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/filters")
public class LegacyFilterController {

    private final FilterDAO filterDao;

    public LegacyFilterController(FilterDAO filterDao) {
        this.filterDao = filterDao;
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<FilterDTO>> listFilters() {
        List<FilterDTO> result = new ArrayList<>();
        for (Object obj : filterDao.findAll()) {
            FilterBean bean = (FilterBean) obj;
            FilterDTO dto = new FilterDTO();
            dto.setId(bean.getId());
            dto.setName(bean.getName());
            dto.setDescription(bean.getDescription());
            dto.setOwnerId(bean.getOwnerId());
            dto.setDateCreated(bean.getCreatedDate());
            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilterDTO> getFilter(@PathVariable int id) {
        FilterBean bean = (FilterBean) filterDao.findByPK(id);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        FilterDTO dto = new FilterDTO();
        dto.setId(bean.getId());
        dto.setName(bean.getName());
        dto.setDescription(bean.getDescription());
        dto.setOwnerId(bean.getOwnerId());
        dto.setDateCreated(bean.getCreatedDate());
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<FilterDTO> createFilter(@RequestBody FilterDTO request) {
        FilterBean bean = new FilterBean();
        bean.setName(request.getName());
        bean.setDescription(request.getDescription() != null ? request.getDescription() : "");
        bean = (FilterBean) filterDao.create(bean);
        FilterDTO dto = new FilterDTO();
        dto.setId(bean.getId());
        dto.setName(bean.getName());
        dto.setDescription(bean.getDescription());
        dto.setOwnerId(bean.getOwnerId());
        dto.setDateCreated(bean.getCreatedDate());
        return ResponseEntity.ok(dto);
    }

    public static class FilterDTO {
        private int id;
        private String name;
        private String description;
        private int ownerId;
        private java.util.Date dateCreated;

        public int getId() { return id; }
        public void setId(int v) { this.id = v; }
        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
        public String getDescription() { return description; }
        public void setDescription(String v) { this.description = v; }
        public int getOwnerId() { return ownerId; }
        public void setOwnerId(int v) { this.ownerId = v; }
        public java.util.Date getDateCreated() { return dateCreated; }
        public void setDateCreated(java.util.Date v) { this.dateCreated = v; }
    }
}

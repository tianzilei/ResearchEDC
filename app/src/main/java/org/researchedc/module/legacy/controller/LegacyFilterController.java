package org.researchedc.module.legacy.controller;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.researchedc.module.filter.entity.FilterEntity;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.filter.service.FilterService;
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

    private final FilterService filterService;
    private final CurrentUserUtils currentUserUtils;

    public LegacyFilterController(FilterService filterService, CurrentUserUtils currentUserUtils) {
        this.filterService = filterService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping
    public ResponseEntity<List<FilterDTO>> listFilters() {
        List<FilterDTO> result = new ArrayList<>();
        for (FilterEntity entity : filterService.listAll()) {
            result.add(toDto(entity));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilterDTO> getFilter(@PathVariable int id) {
        try {
            FilterEntity entity = filterService.getById(id);
            return ResponseEntity.ok(toDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<FilterDTO> createFilter(@RequestBody FilterDTO request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        FilterEntity entity = filterService.create(request.getName(), request.getDescription(), ownerId);
        return ResponseEntity.ok(toDto(entity));
    }

    private static FilterDTO toDto(FilterEntity entity) {
        FilterDTO dto = new FilterDTO();
        dto.setId(entity.getFilterId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setOwnerId(entity.getOwnerId() != null ? entity.getOwnerId() : 0);
        if (entity.getDateCreated() != null) {
            dto.setDateCreated(Date.from(
                    entity.getDateCreated().atZone(ZoneId.systemDefault()).toInstant()));
        }
        return dto;
    }

    public static class FilterDTO {
        private int id;
        private String name;
        private String description;
        private int ownerId;
        private Date dateCreated;

        public int getId() { return id; }
        public void setId(int v) { this.id = v; }
        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
        public String getDescription() { return description; }
        public void setDescription(String v) { this.description = v; }
        public int getOwnerId() { return ownerId; }
        public void setOwnerId(int v) { this.ownerId = v; }
        public Date getDateCreated() { return dateCreated; }
        public void setDateCreated(Date v) { this.dateCreated = v; }
    }
}

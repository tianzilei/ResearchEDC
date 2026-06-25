package org.researchedc.module.filter.controller;

import java.util.List;

import org.researchedc.module.filter.dto.CreateFilterRequest;
import org.researchedc.module.filter.dto.FilterDTO;
import org.researchedc.module.filter.entity.FilterEntity;
import org.researchedc.module.filter.service.FilterService;
import org.researchedc.config.CurrentUserUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/filters")
public class FilterController {

    private final FilterService filterService;
    private final CurrentUserUtils currentUserUtils;

    public FilterController(FilterService filterService, CurrentUserUtils currentUserUtils) {
        this.filterService = filterService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping
    public ResponseEntity<List<FilterDTO>> listFilters() {
        return ResponseEntity.ok(filterService.listAll().stream().map(this::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilterDTO> getFilter(@PathVariable int id) {
        try {
            return ResponseEntity.ok(toDto(filterService.getById(id)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<FilterDTO> createFilter(@RequestBody CreateFilterRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        FilterEntity entity = filterService.create(request.getName(), request.getDescription(), ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(entity));
    }

    private FilterDTO toDto(FilterEntity entity) {
        FilterDTO dto = new FilterDTO();
        dto.setFilterId(entity.getFilterId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setOwnerId(entity.getOwnerId());
        dto.setDateCreated(entity.getDateCreated());
        return dto;
    }
}

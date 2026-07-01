package org.researchedc.module.filter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.filter.entity.FilterEntity;
import org.researchedc.module.filter.repository.FilterRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FilterService {

    private final FilterRepository filterRepository;
    private final CurrentStudyAccessService currentStudyAccessService;

    public FilterService(FilterRepository filterRepository,
                         CurrentStudyAccessService currentStudyAccessService) {
        this.filterRepository = filterRepository;
        this.currentStudyAccessService = currentStudyAccessService;
    }

    public List<FilterEntity> listAll(Integer currentUserId) {
        if (currentStudyAccessService.canReadAllStudies(currentUserId)) {
            return filterRepository.findAll();
        }
        return filterRepository.findByOwnerIdOrderByNameAsc(currentUserId);
    }

    public FilterEntity getById(Integer id, Integer currentUserId) {
        FilterEntity entity = filterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Filter not found: " + id));
        requireOwnerOrAdministrator(currentUserId, entity);
        return entity;
    }

    @Transactional
    public FilterEntity create(String name, String description, Integer ownerId) {
        FilterEntity entity = new FilterEntity();
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setStatusId(1);
        entity.setOwnerId(ownerId);
        entity.setDateCreated(LocalDateTime.now());
        return filterRepository.save(entity);
    }

    @Transactional
    public FilterEntity update(Integer id, String name, String description, Integer currentUserId) {
        FilterEntity entity = filterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Filter not found: " + id));
        requireOwnerOrAdministrator(currentUserId, entity);
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setDateUpdated(LocalDateTime.now());
        return filterRepository.save(entity);
    }

    private void requireOwnerOrAdministrator(Integer currentUserId, FilterEntity entity) {
        if (currentStudyAccessService.canReadAllStudies(currentUserId)) {
            return;
        }
        if (!Objects.equals(currentUserId, entity.getOwnerId())) {
            throw new AccessDeniedException("You do not have access to this filter");
        }
    }
}

package org.researchedc.module.filter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.module.filter.entity.FilterEntity;
import org.researchedc.module.filter.repository.FilterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FilterService {

    private final FilterRepository filterRepository;

    public FilterService(FilterRepository filterRepository) {
        this.filterRepository = filterRepository;
    }

    public List<FilterEntity> listAll() {
        return filterRepository.findAll();
    }

    public FilterEntity getById(Integer id) {
        return filterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Filter not found: " + id));
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
    public FilterEntity update(Integer id, String name, String description) {
        FilterEntity entity = filterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Filter not found: " + id));
        entity.setName(name);
        entity.setDescription(description != null ? description : "");
        entity.setDateUpdated(LocalDateTime.now());
        return filterRepository.save(entity);
    }
}

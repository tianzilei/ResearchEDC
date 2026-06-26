package org.researchedc.module.filter.repository;

import org.researchedc.module.filter.entity.FilterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilterRepository extends JpaRepository<FilterEntity, Integer> {

    List<FilterEntity> findByStatusId(Integer statusId);
}

package org.researchedc.module.dataset.repository;

import org.researchedc.module.dataset.entity.DatasetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetRepository extends JpaRepository<DatasetEntity, Integer> {

    List<DatasetEntity> findByStudyId(Integer studyId);
}

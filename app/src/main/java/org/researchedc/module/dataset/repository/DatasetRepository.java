package org.researchedc.module.dataset.repository;

import org.researchedc.module.dataset.entity.DatasetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetRepository extends JpaRepository<DatasetEntity, Integer> {

    List<DatasetEntity> findByStudyId(Integer studyId);

    List<DatasetEntity> findByStatusId(Integer statusId);

    List<DatasetEntity> findByStudyIdAndStatusId(Integer studyId, Integer statusId);

    List<DatasetEntity> findByNameAndStudyId(String name, Integer studyId);

    List<DatasetEntity> findByStudyIdOrderByName(Integer studyId);

    List<DatasetEntity> findTop5ByStudyIdOrderByDatasetId(Integer studyId);

    List<DatasetEntity> findByOwnerIdAndStudyId(Integer ownerId, Integer studyId);

    List<DatasetEntity> findAllByOrderByStudyIdAscNameAsc();
}

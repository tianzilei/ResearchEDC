package org.researchedc.module.discrepancynote.repository;

import java.util.List;
import org.researchedc.module.discrepancynote.entity.DiscrepancyNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscrepancyNoteRepository extends JpaRepository<DiscrepancyNoteEntity, Integer> {

    List<DiscrepancyNoteEntity> findByStudyId(Integer studyId);

    List<DiscrepancyNoteEntity> findByParentDnId(Integer parentDnId);

    List<DiscrepancyNoteEntity> findByEntityTypeAndEntityId(String entityType, Integer entityId);

    List<DiscrepancyNoteEntity> findByStudyIdAndParentDnId(Integer studyId, Integer parentDnId);

    List<DiscrepancyNoteEntity> findByStudyIdAndParentDnIdIsNull(Integer studyId);

    List<DiscrepancyNoteEntity> findByEntityTypeAndEntityIdAndParentDnIdIsNull(String entityType, Integer entityId);

    long countByResolutionStatusIdNot(Integer resolutionStatusId);
}

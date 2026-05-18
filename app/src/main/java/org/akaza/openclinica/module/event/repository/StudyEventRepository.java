package org.akaza.openclinica.module.event.repository;

import java.util.List;
import org.akaza.openclinica.module.event.entity.StudyEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyEventRepository extends JpaRepository<StudyEventEntity, Integer> {

    List<StudyEventEntity> findByStudySubjectIdOrderByDateStart(Integer studySubjectId);

    List<StudyEventEntity> findByStudyEventDefinitionId(Integer studyEventDefinitionId);
}

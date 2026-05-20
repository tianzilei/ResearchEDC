package org.researchedc.module.event.repository;

import java.util.List;
import org.researchedc.module.event.entity.StudyEventDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyEventDefinitionRepository extends JpaRepository<StudyEventDefinitionEntity, Integer> {

    List<StudyEventDefinitionEntity> findByStudyIdOrderByName(Integer studyId);
}

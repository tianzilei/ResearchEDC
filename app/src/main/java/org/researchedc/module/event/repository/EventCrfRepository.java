package org.researchedc.module.event.repository;

import java.util.List;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCrfRepository extends JpaRepository<EventCrfEntity, Integer> {

    List<EventCrfEntity> findByStudyEventId(Integer studyEventId);

    List<EventCrfEntity> findByStudySubjectId(Integer studySubjectId);

    long countByDateCompletedIsNull();
}

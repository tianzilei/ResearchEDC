package org.researchedc.module.studybuild.repository;

import java.util.List;
import org.researchedc.module.studybuild.entity.StudyTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTemplateRepository extends JpaRepository<StudyTemplateEntity, Long> {
    List<StudyTemplateEntity> findByActiveTrueOrderByName();
}

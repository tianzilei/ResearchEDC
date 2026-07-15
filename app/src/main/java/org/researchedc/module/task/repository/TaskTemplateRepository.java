package org.researchedc.module.task.repository;

import java.util.List;
import org.researchedc.module.task.entity.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {
    List<TaskTemplate> findByStudyIdOrderByNameAsc(Integer studyId);
}

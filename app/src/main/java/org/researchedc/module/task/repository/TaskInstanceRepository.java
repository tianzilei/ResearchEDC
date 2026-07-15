package org.researchedc.module.task.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.researchedc.module.task.entity.TaskInstance;
import org.researchedc.module.task.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskInstanceRepository extends JpaRepository<TaskInstance, Long> {
    List<TaskInstance> findByStudyIdOrderByDueDateAscCreatedDateAsc(Integer studyId);

    List<TaskInstance> findByStudyIdInOrderByDueDateAscCreatedDateAsc(Collection<Integer> studyIds);

    List<TaskInstance> findByStudyIdAndStatusOrderByDueDateAscCreatedDateAsc(Integer studyId,
                                                                              TaskStatus status);

    List<TaskInstance> findByAssignedToAndStatusInOrderByDueDateAscCreatedDateAsc(Integer assignedTo,
                                                                                   Collection<TaskStatus> statuses);

    List<TaskInstance> findByStatusInAndDueDateBefore(Collection<TaskStatus> statuses,
                                                       LocalDateTime dueDate);
}

package org.researchedc.module.task.service;

import org.researchedc.module.task.entity.TaskInstance;

public interface TaskReminderDispatcher {
    void dispatchReminder(TaskInstance task);
}

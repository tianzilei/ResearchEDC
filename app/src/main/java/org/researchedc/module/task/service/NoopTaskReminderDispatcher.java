package org.researchedc.module.task.service;

import org.researchedc.module.task.entity.TaskInstance;
import org.springframework.stereotype.Component;

@Component
public class NoopTaskReminderDispatcher implements TaskReminderDispatcher {
    @Override
    public void dispatchReminder(TaskInstance task) {
        // Delivery providers are intentionally plugged in later; no mail path is revived here.
    }
}

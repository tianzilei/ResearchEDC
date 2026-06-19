package org.researchedc.config.scheduler;

import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Customized {@link SchedulerFactoryBean} which replaces the default scheduler implementation by
 * {@link OpenClinicaStdSchedulerFactory}.
 */
public class OpenClinicaSchedulerFactoryBean extends SchedulerFactoryBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setSchedulerFactoryClass(OpenClinicaStdSchedulerFactory.class);
        super.afterPropertiesSet();
    }
}

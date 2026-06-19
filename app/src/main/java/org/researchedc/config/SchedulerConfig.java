package org.researchedc.config;

import java.util.Properties;
import javax.sql.DataSource;

import org.researchedc.config.scheduler.JobExecutionExceptionListener;
import org.researchedc.config.scheduler.JobTriggerListener;
import org.researchedc.config.scheduler.OpenClinicaSchedulerFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ConditionalOnProperty(name = "researchedc.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class SchedulerConfig {

    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

    public SchedulerConfig(DataSource dataSource,
                           PlatformTransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }

    @Bean(destroyMethod = "destroy")
    public OpenClinicaSchedulerFactoryBean schedulerFactoryBean() {
        OpenClinicaSchedulerFactoryBean factory = new OpenClinicaSchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setApplicationContextSchedulerContextKey("applicationContext");

        Properties quartzProps = new Properties();
        quartzProps.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        quartzProps.setProperty("org.quartz.jobStore.class",
            "org.quartz.impl.jdbcjobstore.JobStoreTX");
        quartzProps.setProperty("org.quartz.jobStore.driverDelegateClass",
            "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        quartzProps.setProperty("org.quartz.jobStore.useProperties", "true");
        quartzProps.setProperty("org.quartz.jobStore.tablePrefix", "QUARTZ_");
        quartzProps.setProperty("org.quartz.threadPool.class",
            "org.quartz.simpl.SimpleThreadPool");
        quartzProps.setProperty("org.quartz.threadPool.threadCount", "3");
        quartzProps.setProperty("org.quartz.threadPool.threadPriority", "5");
        factory.setQuartzProperties(quartzProps);

        factory.setGlobalJobListeners(new JobExecutionExceptionListener());
        factory.setGlobalTriggerListeners(new JobTriggerListener());

        return factory;
    }
}

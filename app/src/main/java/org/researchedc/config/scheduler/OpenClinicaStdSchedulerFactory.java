package org.researchedc.config.scheduler;

import java.util.Properties;

import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.ThreadPool;

/**
 * Custom {@link SchedulerFactory} adapted to configure a {@link ThreadPool} with zero threads.
 */
public class OpenClinicaStdSchedulerFactory extends StdSchedulerFactory {

    @Override
    public void initialize(Properties props) throws SchedulerException {
        String threadCount = props.getProperty("org.quartz.threadPool.threadCount");
        if (threadCount != null && threadCount.trim().equals("0")) {
            props.put("org.quartz.threadPool.class", "org.quartz.simpl.ZeroSizeThreadPool");
            props.remove("org.quartz.threadPool.threadCount");
            props.remove("org.quartz.threadPool.threadPriority");
        }
        super.initialize(props);
    }
}

package org.akaza.openclinica.web.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

/**
 * establishing stateful-ness on the Java side to avoid locking, etc
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class ExampleStatefulJob
    extends ExampleSpringJob {

    public ExampleStatefulJob() {
        super();
    }
}

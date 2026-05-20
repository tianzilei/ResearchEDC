package org.researchedc.web.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

/**
 * Import Stateful Job, by Tom Hickerson 04/2009
 * Establishing stateful-ness on the Java side to avoid locking
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class ImportStatefulJob
    extends ImportSpringJob {

    public ImportStatefulJob() {
        super();
    }
}

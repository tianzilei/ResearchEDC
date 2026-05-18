/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class JobExecutionExceptionListener implements JobListener {

    private static final Logger LOG = LoggerFactory.getLogger(JobExecutionExceptionListener.class);

    @Override
    public String getName() {
        return "JobExecutionExceptionListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        // no-op
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // no-op
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        if (jobException != null) {
            LOG.warn("Error executing Quartz job", jobException);
        }
    }

}

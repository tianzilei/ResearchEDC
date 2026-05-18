/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.job;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class JobTriggerListener implements TriggerListener {

    private static final Logger LOG = LoggerFactory.getLogger(JobTriggerListener.class);

    @Override
    public String getName() {
        return "JobTriggerListener";
    }

	@Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        logTriggerInfo(trigger, "Trigger {} fired");
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        logTriggerInfo(trigger, "Trigger {} vetoed");
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        logTriggerInfo(trigger, "Trigger {} misfired");
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        logTriggerInfo(trigger, "Trigger {} complete");
    }

    private void logTriggerInfo(Trigger trigger, String message) {
        LOG.debug(message, trigger.getKey().getName());
    }



}

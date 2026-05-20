package org.researchedc.web.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class XalanStatefulJob extends XalanTransformJob {
    
    public XalanStatefulJob() {
        super();
    }

}

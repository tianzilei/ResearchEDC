package org.researchedc.web.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class XalanStatefulJob extends XalanTransformJob {
    
    public XalanStatefulJob() {
        super();
    }

}

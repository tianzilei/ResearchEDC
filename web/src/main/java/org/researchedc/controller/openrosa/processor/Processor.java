package org.researchedc.controller.openrosa.processor;

import org.researchedc.controller.openrosa.SubmissionContainer;
import org.springframework.beans.factory.annotation.Autowired;

public interface Processor {

    public void process(SubmissionContainer container) throws Exception;

}

package org.researchedc.controller.openrosa.processor;

import org.researchedc.controller.openrosa.SubmissionContainer;

public interface Processor {

    public void process(SubmissionContainer container) throws Exception;

}

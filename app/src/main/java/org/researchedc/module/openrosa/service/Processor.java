package org.researchedc.module.openrosa.service;

public interface Processor {
    int DEFAULT_ORDER = 0;

    void process(SubmissionContext ctx) throws Exception;
}

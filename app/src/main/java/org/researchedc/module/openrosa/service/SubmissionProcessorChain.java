package org.researchedc.module.openrosa.service;

import java.util.Collections;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

@Component
public class SubmissionProcessorChain {

    private final List<Processor> processors;

    public SubmissionProcessorChain(List<Processor> processors) {
        this.processors = processors;
    }

    @PostConstruct
    public void init() {
        Collections.sort(processors, AnnotationAwareOrderComparator.INSTANCE);
    }

    public void process(SubmissionContext ctx) throws Exception {
        for (Processor processor : processors) {
            processor.process(ctx);
            if (ctx.hasErrors()) {
                return;
            }
        }
    }
}

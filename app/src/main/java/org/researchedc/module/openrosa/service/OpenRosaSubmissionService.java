package org.researchedc.module.openrosa.service;

import java.util.Optional;

import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.openrosa.dto.SubmissionRequest;
import org.researchedc.module.openrosa.dto.SubmissionResponse;
import org.researchedc.module.openrosa.internal.adapter.OpenRosaCrfAdapter;
import org.researchedc.module.study.entity.StudyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpenRosaSubmissionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final OpenRosaCrfAdapter adapter;
    private final SubmissionProcessorChain processorChain;

    public OpenRosaSubmissionService(OpenRosaCrfAdapter adapter,
                                      SubmissionProcessorChain processorChain) {
        this.adapter = adapter;
        this.processorChain = processorChain;
    }

    @Transactional
    public SubmissionResponse processSubmission(SubmissionRequest request) {
        try {
            Optional<StudyEntity> studyOpt = adapter.findStudyByOcOid(request.getStudyOid());
            if (studyOpt.isEmpty()) {
                return SubmissionResponse.error("Study not found: " + request.getStudyOid());
            }
            StudyEntity study = studyOpt.get();

            Optional<CrfVersionEntity> cvOpt = adapter.findCrfVersionByOcOid(request.getCrfVersionOid());
            if (cvOpt.isEmpty()) {
                return SubmissionResponse.error("CRF version not found: " + request.getCrfVersionOid());
            }
            CrfVersionEntity crfVersion = cvOpt.get();

            SubmissionContext ctx = new SubmissionContext();
            ctx.setStudy(study);
            ctx.setCrfVersion(crfVersion);
            ctx.setRequest(request);
            ctx.setRequestBody(request.getXmlSubmission());
            ctx.getSubjectContextMap().put("studySubjectOID", request.getStudySubjectOid());
            ctx.getSubjectContextMap().put("studyEventDefinitionID", request.getStudyEventDefinitionId());
            ctx.getSubjectContextMap().put("studyEventOrdinal", request.getStudyEventOrdinal());
            ctx.getSubjectContextMap().put("crfVersionOID", request.getCrfVersionOid());

            processorChain.process(ctx);

            if (ctx.hasErrors()) {
                String errorMsg = String.join("; ", ctx.getErrors());
                logger.warn("Submission validation errors: {}", errorMsg);
                return SubmissionResponse.error(errorMsg);
            }

            logger.info("Submission processed successfully for study {}, form {}",
                    request.getStudyOid(), request.getCrfVersionOid());
            return SubmissionResponse.success();
        } catch (Exception e) {
            logger.error("Submission processing failed", e);
            return SubmissionResponse.error("Internal error: " + e.getMessage());
        }
    }
}

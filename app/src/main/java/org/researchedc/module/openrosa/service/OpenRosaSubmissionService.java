package org.researchedc.module.openrosa.service;

import java.io.StringReader;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.openrosa.dto.SubmissionRequest;
import org.researchedc.module.openrosa.dto.SubmissionResponse;
import org.researchedc.module.openrosa.internal.adapter.OpenRosaCrfAdapter;
import org.researchedc.module.study.entity.StudyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

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

            String crfVersionOid = firstPresent(request.getCrfVersionOid(), extractCrfVersionOid(request.getXmlSubmission()));
            request.setCrfVersionOid(crfVersionOid);

            Optional<CrfVersionEntity> cvOpt = adapter.findCrfVersionByOcOid(crfVersionOid);
            if (cvOpt.isEmpty()) {
                return SubmissionResponse.error("CRF version not found: " + crfVersionOid);
            }
            CrfVersionEntity crfVersion = cvOpt.get();

            SubmissionContext ctx = new SubmissionContext();
            ctx.setStudy(study);
            ctx.setCrfVersion(crfVersion);
            ctx.setRequest(request);
            ctx.setRequestBody(request.getXmlSubmission());
            ctx.getSubjectContextMap().put("studyOid", request.getStudyOid());
            ctx.getSubjectContextMap().put("studySubjectOid", request.getStudySubjectOid());
            ctx.getSubjectContextMap().put("studySubjectOID", request.getStudySubjectOid());
            ctx.getSubjectContextMap().put("studyEventDefinitionId", request.getStudyEventDefinitionId());
            ctx.getSubjectContextMap().put("studyEventDefinitionID", request.getStudyEventDefinitionId());
            ctx.getSubjectContextMap().put("studyEventOrdinal", request.getStudyEventOrdinal());
            ctx.getSubjectContextMap().put("crfVersionOid", request.getCrfVersionOid());
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

    private String firstPresent(String explicitValue, String fallbackValue) {
        if (explicitValue != null && !explicitValue.isBlank()) {
            return explicitValue;
        }
        return fallbackValue;
    }

    private String extractCrfVersionOid(String xml) {
        if (xml == null || xml.isBlank()) {
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            String formId = doc.getDocumentElement().getAttribute("id");
            return formId == null || formId.isBlank() ? null : formId;
        } catch (Exception e) {
            logger.warn("Unable to extract CRF version OID from OpenRosa submission XML", e);
            return null;
        }
    }
}

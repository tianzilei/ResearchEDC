package org.researchedc.module.openrosa.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.openrosa.dto.SubmissionRequest;
import org.researchedc.module.openrosa.dto.SubmissionResponse;
import org.researchedc.module.openrosa.internal.adapter.OpenRosaCrfAdapter;
import org.researchedc.module.openrosa.service.OpenRosaFormService;
import org.researchedc.module.openrosa.service.OpenRosaSubmissionService;
import org.researchedc.module.study.entity.StudyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/openrosa")
public class OpenRosaController {

    private static final String FORM_CONTEXT = "ecid";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final OpenRosaCrfAdapter adapter;
    private final OpenRosaFormService formService;
    private final OpenRosaSubmissionService submissionService;

    public OpenRosaController(OpenRosaCrfAdapter adapter,
                              OpenRosaFormService formService,
                              OpenRosaSubmissionService submissionService) {
        this.adapter = adapter;
        this.formService = formService;
        this.submissionService = submissionService;
    }

    @GetMapping(value = "/{studyOid}/formList", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> formList(@PathVariable String studyOid) {
        Optional<StudyEntity> studyOpt = adapter.findStudyByOcOid(studyOid);
        if (studyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String xml = formService.buildFormListXml(studyOid);
        return ResponseEntity.ok(xml);
    }

    @GetMapping(value = "/{studyOid}/xform/{formId}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> xform(@PathVariable String studyOid,
                                         @PathVariable String formId) {
        Optional<StudyEntity> studyOpt = adapter.findStudyByOcOid(studyOid);
        if (studyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<CrfVersionEntity> cvOpt = adapter.findCrfVersionByOcOid(formId);
        if (cvOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String xml = formService.buildXFormXml(formId);
        if (xml == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(xml);
    }

    @GetMapping(value = "/{studyOid}/xformsManifest/{formId}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> manifest(@PathVariable String studyOid,
                                            @PathVariable String formId) {
        Optional<StudyEntity> studyOpt = adapter.findStudyByOcOid(studyOid);
        if (studyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String xml = formService.buildManifestXml(formId);
        return ResponseEntity.ok(xml);
    }

    @PostMapping("/{studyOid}/submission")
    public ResponseEntity<String> postSubmission(
            @PathVariable String studyOid,
            @RequestParam(FORM_CONTEXT) String ecid,
            HttpServletRequest request) {
        try {
            Optional<StudyEntity> studyOpt = adapter.findStudyByOcOid(studyOid);
            if (studyOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = request.getContentType();
            String xmlBody;
            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                // For multipart, extract the xml_submission_file part
                xmlBody = new String(request.getPart("xml_submission_file").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } else {
                xmlBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }

            SubmissionRequest req = new SubmissionRequest();
            req.setStudyOid(studyOid);
            req.setXmlSubmission(xmlBody);
            req.setEcid(ecid);

            SubmissionResponse resp = submissionService.processSubmission(req);
            if (resp.getStatusCode() == 201) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(resp.toXml());
            }
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(resp.toXml());
        } catch (Exception e) {
            logger.error("Submission failed for studyOid={}, ecid={}", studyOid, ecid, e);
            SubmissionResponse resp = SubmissionResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(resp.toXml());
        }
    }

    @RequestMapping(value = "/{studyOid}/submission", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headSubmission(@PathVariable String studyOid) {
        return ResponseEntity.ok()
                .header("X-OpenRosa-Version", "1.0")
                .header("X-OpenRosa-Accept-Content-Length", "10485760")
                .build();
    }
}

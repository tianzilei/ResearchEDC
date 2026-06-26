package org.researchedc.module.openrosa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.openrosa.dto.SubmissionRequest;
import org.researchedc.module.openrosa.dto.SubmissionResponse;
import org.researchedc.module.openrosa.internal.adapter.OpenRosaCrfAdapter;
import org.researchedc.module.study.entity.StudyEntity;

@ExtendWith(MockitoExtension.class)
class OpenRosaSubmissionServiceTest {

    @Mock private OpenRosaCrfAdapter adapter;
    @Mock private SubmissionProcessorChain processorChain;

    private OpenRosaSubmissionService service;

    @BeforeEach
    void setUp() {
        service = new OpenRosaSubmissionService(adapter, processorChain);
    }

    @Test
    void processSubmission_extractsCrfVersionOidFromXFormRootAndPopulatesContext() throws Exception {
        StudyEntity study = study();
        CrfVersionEntity crfVersion = crfVersion("F_DEMO_V1");
        SubmissionRequest request = request(null, "<data id=\"F_DEMO_V1\"><I_WEIGHT>70</I_WEIGHT></data>");

        when(adapter.findStudyByOcOid("S_DEMO")).thenReturn(Optional.of(study));
        when(adapter.findCrfVersionByOcOid("F_DEMO_V1")).thenReturn(Optional.of(crfVersion));
        doAnswer(invocation -> {
            SubmissionContext context = invocation.getArgument(0);
            assertSame(study, context.getStudy());
            assertSame(crfVersion, context.getCrfVersion());
            assertEquals("S_DEMO", context.getSubjectContextMap().get("studyOid"));
            assertEquals("SS_DEMO", context.getSubjectContextMap().get("studySubjectOid"));
            assertEquals("SE_DEMO", context.getSubjectContextMap().get("studyEventDefinitionId"));
            assertEquals("1", context.getSubjectContextMap().get("studyEventOrdinal"));
            assertEquals("F_DEMO_V1", context.getSubjectContextMap().get("crfVersionOid"));
            assertEquals("F_DEMO_V1", context.getSubjectContextMap().get("crfVersionOID"));
            return null;
        }).when(processorChain).process(any(SubmissionContext.class));

        SubmissionResponse response = service.processSubmission(request);

        assertEquals(201, response.getStatusCode());
        assertEquals("F_DEMO_V1", request.getCrfVersionOid());
        verify(adapter).findCrfVersionByOcOid("F_DEMO_V1");
    }

    @Test
    void processSubmission_prefersExplicitCrfVersionOidOverXmlRoot() {
        SubmissionRequest request = request("F_EXPLICIT", "<data id=\"F_XML\"><I_WEIGHT>70</I_WEIGHT></data>");

        when(adapter.findStudyByOcOid("S_DEMO")).thenReturn(Optional.of(study()));
        when(adapter.findCrfVersionByOcOid("F_EXPLICIT")).thenReturn(Optional.of(crfVersion("F_EXPLICIT")));

        SubmissionResponse response = service.processSubmission(request);

        assertEquals(201, response.getStatusCode());
        assertEquals("F_EXPLICIT", request.getCrfVersionOid());
        verify(adapter).findCrfVersionByOcOid("F_EXPLICIT");
        verify(adapter, never()).findCrfVersionByOcOid("F_XML");
    }

    private SubmissionRequest request(String crfVersionOid, String xml) {
        SubmissionRequest request = new SubmissionRequest();
        request.setStudyOid("S_DEMO");
        request.setStudySubjectOid("SS_DEMO");
        request.setStudyEventDefinitionId("SE_DEMO");
        request.setStudyEventOrdinal("1");
        request.setCrfVersionOid(crfVersionOid);
        request.setXmlSubmission(xml);
        return request;
    }

    private StudyEntity study() {
        StudyEntity study = new StudyEntity();
        study.setStudyId(1);
        study.setOcOid("S_DEMO");
        return study;
    }

    private CrfVersionEntity crfVersion(String oid) {
        CrfVersionEntity crfVersion = new CrfVersionEntity();
        crfVersion.setCrfVersionId(2);
        crfVersion.setOcOid(oid);
        return crfVersion;
    }
}

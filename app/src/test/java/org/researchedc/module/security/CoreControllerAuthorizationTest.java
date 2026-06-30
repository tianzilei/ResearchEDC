package org.researchedc.module.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.module.crf.controller.CrfManageController;
import org.researchedc.module.datacapture.controller.DataCaptureController;
import org.researchedc.module.dataimport.controller.ImportController;
import org.researchedc.module.discrepancynote.controller.DiscrepancyNoteController;
import org.researchedc.module.event.controller.EventController;
import org.researchedc.module.export.controller.ExportController;
import org.researchedc.module.randomization.controller.RandomizationController;
import org.researchedc.module.rule.controller.RuleController;
import org.researchedc.module.study.controller.StudyController;
import org.researchedc.module.subject.controller.SubjectController;
import org.researchedc.module.subjectgroup.controller.SubjectGroupController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

class CoreControllerAuthorizationTest {

    @ParameterizedTest
    @MethodSource("securedControllerMethods")
    void coreControllerMethodsDeclareExpectedAuthorization(Method method, String expectedExpression) {
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize, method::toGenericString);
        assertEquals(expectedExpression, preAuthorize.value(), method::toGenericString);
    }

    private static Stream<Arguments> securedControllerMethods() throws NoSuchMethodException {
        return Stream.of(
                secured(StudyController.class, "createStudy",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES,
                        org.researchedc.module.study.dto.CreateStudyRequest.class),
                secured(StudyController.class, "updateStudy",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES,
                        Integer.class, org.researchedc.module.study.dto.UpdateStudyRequest.class),
                secured(StudyController.class, "deleteStudy",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES, Integer.class),
                secured(StudyController.class, "updateStudyStatus",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES, Integer.class, Integer.class),
                secured(StudyController.class, "updateFeatureFlags",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES,
                        Integer.class, org.researchedc.module.study.dto.FeatureFlagsDTO.class),

                secured(SubjectController.class, "createSubject",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        org.researchedc.module.subject.dto.CreateSubjectRequest.class),
                secured(SubjectController.class, "enrollSubject",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        org.researchedc.module.subject.dto.EnrollSubjectRequest.class),
                secured(SubjectController.class, "signStudySubject",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        Integer.class, org.researchedc.module.subject.dto.SignSubjectRequest.class),
                secured(SubjectController.class, "reassignStudySubject",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        Integer.class, org.researchedc.module.subject.dto.ReassignStudySubjectRequest.class),
                secured(SubjectController.class, "removeSubject",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Integer.class),
                secured(SubjectController.class, "restoreSubject",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Integer.class),
                secured(SubjectController.class, "removeStudySubject",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Integer.class),
                secured(SubjectController.class, "restoreStudySubject",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Integer.class),

                secured(EventController.class, "createEventDefinition",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES,
                        org.researchedc.module.event.dto.CreateEventDefinitionRequest.class),
                secured(EventController.class, "removeEventDefinition",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES, Integer.class),
                secured(EventController.class, "restoreEventDefinition",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES, Integer.class),
                secured(EventController.class, "scheduleEvent",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        org.researchedc.module.event.dto.ScheduleEventRequest.class),
                secured(EventController.class, "updateEvent",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        Integer.class, org.researchedc.module.event.dto.UpdateEventRequest.class),
                secured(EventController.class, "completeEvent",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Integer.class),
                secured(EventController.class, "removeStudyEvent",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Integer.class),
                secured(EventController.class, "restoreStudyEvent",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Integer.class),
                secured(EventController.class, "removeEventCrfById",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Integer.class),
                secured(EventController.class, "restoreEventCrfById",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Integer.class),
                secured(EventController.class, "removeEventCrf",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Integer.class, Integer.class),

                secured(DataCaptureController.class, "getItemData",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, Integer.class),
                secured(DataCaptureController.class, "saveItemData",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        org.researchedc.module.datacapture.dto.SaveItemDataRequest.class),
                secured(DataCaptureController.class, "batchSaveItems",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        org.researchedc.module.datacapture.dto.BatchSaveItemsRequest.class),
                secured(DataCaptureController.class, "evaluateRules",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, int.class),
                secured(DataCaptureController.class, "downloadAttachmentByEventCrf",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA,
                        int.class, String.class, jakarta.servlet.http.HttpServletResponse.class),
                secured(DataCaptureController.class, "listAttachmentsByEventCrf",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, int.class),
                secured(DataCaptureController.class, "uploadAttachment",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, int.class, MultipartFile.class),

                secured(DiscrepancyNoteController.class, "listNotes",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, Integer.class, Integer.class),
                secured(DiscrepancyNoteController.class, "getNote",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, int.class),
                secured(DiscrepancyNoteController.class, "createNote",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        org.researchedc.module.discrepancynote.dto.CreateDiscrepancyNoteRequest.class),
                secured(DiscrepancyNoteController.class, "resolveNote",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, int.class),

                secured(RuleController.class, "listRuleSets",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, Integer.class),
                secured(RuleController.class, "getRuleSet",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, int.class),
                secured(RuleController.class, "addRuleToRuleSet",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        int.class, org.researchedc.module.rule.dto.AddRuleToRuleSetRequest.class),
                secured(RuleController.class, "removeRuleFromRuleSet",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, int.class, int.class),
                secured(RuleController.class, "getRule",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, int.class),
                secured(RuleController.class, "createRule",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        org.researchedc.module.rule.dto.CreateRuleRequest.class),
                secured(RuleController.class, "updateRule",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        int.class, org.researchedc.module.rule.dto.CreateRuleRequest.class),
                secured(RuleController.class, "deleteRule",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, int.class),

                secured(SubjectGroupController.class, "listClasses",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, int.class),
                secured(SubjectGroupController.class, "getClass",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, int.class),
                secured(SubjectGroupController.class, "listGroups",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, int.class),
                secured(SubjectGroupController.class, "createClass",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        org.researchedc.module.subjectgroup.dto.CreateGroupClassRequest.class),
                secured(SubjectGroupController.class, "updateClass",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        int.class, org.researchedc.module.subjectgroup.dto.CreateGroupClassRequest.class),
                secured(SubjectGroupController.class, "createGroup",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        int.class, org.researchedc.module.subjectgroup.dto.CreateGroupRequest.class),
                secured(SubjectGroupController.class, "updateGroup",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        int.class, org.researchedc.module.subjectgroup.dto.CreateGroupRequest.class),

                secured(RandomizationController.class, "listSchemes",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, Integer.class),
                secured(RandomizationController.class, "getScheme",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, Long.class),
                secured(RandomizationController.class, "listAssignments",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, Long.class),
                secured(RandomizationController.class, "getAssignment",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, Long.class, Integer.class),
                secured(RandomizationController.class, "listUnblindingRequests",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, Long.class),
                secured(RandomizationController.class, "listPendingRequests",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA),
                secured(RandomizationController.class, "getAuditLogs",
                        CoreEdcAuthorityExpressions.READ_EDC_DATA, Long.class, Integer.class),
                secured(RandomizationController.class, "createScheme",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        org.researchedc.module.randomization.dto.SchemeDTO.class, Integer.class),
                secured(RandomizationController.class, "updateScheme",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        Long.class, org.researchedc.module.randomization.dto.SchemeDTO.class, Integer.class),
                secured(RandomizationController.class, "activateScheme",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Long.class, Integer.class),
                secured(RandomizationController.class, "closeScheme",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA, Long.class, Integer.class),
                secured(RandomizationController.class, "randomize",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        org.researchedc.module.randomization.dto.RandomizeRequest.class, Integer.class),
                secured(RandomizationController.class, "requestUnblinding",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        Long.class, Integer.class, String.class),
                secured(RandomizationController.class, "reviewUnblinding",
                        CoreEdcAuthorityExpressions.WRITE_EDC_DATA,
                        Long.class, org.researchedc.module.randomization.enums.UnblindingStatus.class,
                        Integer.class, String.class),

                secured(ExportController.class, "createJob",
                        CoreEdcAuthorityExpressions.EXPORT_DATA,
                        org.researchedc.module.export.dto.CreateExportJobRequest.class),
                secured(ExportController.class, "listJobs",
                        CoreEdcAuthorityExpressions.EXPORT_DATA,
                        Integer.class,
                        org.researchedc.module.export.enums.ExportJobStatus.class,
                        org.researchedc.module.export.enums.ExportFormat.class,
                        org.researchedc.module.export.enums.OdmContractVersion.class,
                        Integer.class),
                secured(ExportController.class, "getJob",
                        CoreEdcAuthorityExpressions.EXPORT_DATA, Long.class),
                secured(ExportController.class, "cancelJob",
                        CoreEdcAuthorityExpressions.EXPORT_DATA, Long.class),
                secured(ExportController.class, "retryJob",
                        CoreEdcAuthorityExpressions.EXPORT_DATA, Long.class),
                secured(ExportController.class, "downloadExport",
                        CoreEdcAuthorityExpressions.EXPORT_DATA, Long.class),

                secured(ImportController.class, "createJob",
                        CoreEdcAuthorityExpressions.IMPORT_DATA,
                        org.researchedc.module.dataimport.dto.CreateImportJobRequest.class),
                secured(ImportController.class, "uploadFile",
                        CoreEdcAuthorityExpressions.IMPORT_DATA,
                        MultipartFile.class, String.class, Integer.class, String.class),
                secured(ImportController.class, "listJobs",
                        CoreEdcAuthorityExpressions.IMPORT_DATA, Integer.class),
                secured(ImportController.class, "getJob",
                        CoreEdcAuthorityExpressions.IMPORT_DATA, Long.class),
                secured(ImportController.class, "validate",
                        CoreEdcAuthorityExpressions.IMPORT_DATA, Long.class),
                secured(ImportController.class, "preview",
                        CoreEdcAuthorityExpressions.IMPORT_DATA, Long.class),
                secured(ImportController.class, "commit",
                        CoreEdcAuthorityExpressions.IMPORT_DATA, Long.class),

                secured(CrfManageController.class, "createCrf",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES,
                        org.researchedc.module.crf.dto.CreateCrfRequest.class),
                secured(CrfManageController.class, "updateCrf",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES,
                        int.class, org.researchedc.module.crf.dto.CreateCrfRequest.class),
                secured(CrfManageController.class, "createVersion",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES,
                        int.class, org.researchedc.module.crf.dto.CreateCrfVersionRequest.class),
                secured(CrfManageController.class, "deleteVersion",
                        CoreEdcAuthorityExpressions.ADMINISTER_STUDIES, int.class)
        );
    }

    private static Arguments secured(Class<?> controller, String methodName,
                                     String expectedExpression, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        return Arguments.of(controller.getMethod(methodName, parameterTypes), expectedExpression);
    }
}

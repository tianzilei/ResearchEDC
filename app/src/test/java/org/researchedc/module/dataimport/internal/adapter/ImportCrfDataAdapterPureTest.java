package org.researchedc.module.dataimport.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.core.SubjectEventStatus;
import org.researchedc.bean.submit.crfdata.CRFDataPostImportContainer;
import org.researchedc.bean.submit.crfdata.FormDataBean;
import org.researchedc.bean.submit.crfdata.ODMContainer;
import org.researchedc.bean.submit.crfdata.StudyEventDataBean;
import org.researchedc.bean.submit.crfdata.SubjectDataBean;
import org.researchedc.bean.submit.crfdata.UpsertOnBean;
import org.researchedc.module.dataimport.service.ImportCrfVersionPort;
import org.researchedc.module.dataimport.service.ImportEventCrfPort;
import org.researchedc.module.dataimport.service.ImportItemDataPort;
import org.researchedc.module.dataimport.service.ImportItemFormMetadataPort;
import org.researchedc.module.dataimport.service.ImportItemGroupPort;
import org.researchedc.module.dataimport.service.ImportItemPort;
import org.researchedc.module.dataimport.service.ImportResponseSetPort;
import org.researchedc.module.dataimport.service.ImportStudyEventDefinitionPort;
import org.researchedc.module.dataimport.service.ImportStudyEventPort;
import org.researchedc.module.dataimport.service.ImportStudyLookupPort;
import org.researchedc.module.dataimport.service.ImportStudySubjectPort;

class ImportCrfDataAdapterPureTest {

    @Test
    void validateMetadata_whenStudyMissing_returnsSingleLegacyError() {
        ImportCrfDataAdapter adapter = adapter(
            oid -> null,
            (oid, studyId) -> null,
            (studySubjectId, studyEventDefinitionId, ordinal) -> null,
            (oid, studyId, parentStudyId) -> null,
            oid -> List.of(),
            new NoopEventCrfPort()
        );

        ODMContainer odm = new ODMContainer();
        CRFDataPostImportContainer postImport = new CRFDataPostImportContainer();
        postImport.setStudyOID("S_MISSING");
        odm.setCrfDataPostImportContainer(postImport);

        List<String> errors = adapter.validateMetadata(
            new ImportCrfDataAdapter.ParsedOdm(odm), 1, Locale.ENGLISH);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("S_MISSING"));
    }

    @Test
    void validateEventCrfs_usesModuleOwnedInternalLookupRecords() {
        ImportCrfDataAdapter adapter = adapter(
            oid -> new Object[]{1, 0, "Demo Study"},
            (oid, studyId) -> new Object[]{2, "SS_DEMO"},
            (studySubjectId, studyEventDefinitionId, ordinal) ->
                new Object[]{4, SubjectEventStatus.SCHEDULED.getId(), "HQ"},
            (oid, studyId, parentStudyId) -> new Object[]{3, "Baseline"},
            oid -> List.of(new ImportCrfVersionPort.ImportCrfVersion(5)),
            new ImportEventCrfPort() {
                @Override
                public List<Object[]> findImportEventCrfsByEventSubjectVersion(
                        int studyEventId, int studySubjectId, int crfVersionId) {
                    return List.<Object[]>of(new Object[]{12, 5, Status.AVAILABLE.getId()});
                }

                @Override
                public List<Object[]> findImportEventCrfsByEventSubjectCrfId(
                        int studyEventId, int studySubjectId, int crfVersionId) {
                    return List.of();
                }

                @Override
                public Object[] createImportEventCrf(
                        int studyEventId, int studySubjectId, int crfVersionId,
                        int ownerId, String interviewerName, int statusId) {
                    return new Object[]{99, crfVersionId, statusId};
                }
            }
        );

        ImportCrfDataAdapter.EventCrfValidationResult result =
            adapter.validateEventCrfs(new ImportCrfDataAdapter.ParsedOdm(validatingOdm()), 1, Locale.ENGLISH);

        assertTrue(result.statusesValid());
        assertEquals(1, result.eventCrfCount());
    }

    private ImportCrfDataAdapter adapter(
            ImportStudyLookupPort studyLookupPort,
            ImportStudySubjectPort studySubjectPort,
            ImportStudyEventPort studyEventPort,
            ImportStudyEventDefinitionPort studyEventDefinitionPort,
            ImportCrfVersionPort crfVersionPort,
            ImportEventCrfPort eventCrfPort) {
        return new ImportCrfDataAdapter(
            new NoopItemDataPort(),
            oid -> List.of(),
            oid -> List.of(),
            itemId -> List.of(),
            studyLookupPort,
            studySubjectPort,
            studyEventPort,
            studyEventDefinitionPort,
            crfVersionPort,
            eventCrfPort,
            itemId -> List.of()
        );
    }

    private ODMContainer validatingOdm() {
        FormDataBean form = new FormDataBean();
        form.setFormOID("F_VITALS_V1");

        StudyEventDataBean event = new StudyEventDataBean();
        event.setStudyEventOID("SE_BASELINE");
        event.setStudyEventRepeatKey("1");
        event.setFormData(new ArrayList<>(List.of(form)));

        SubjectDataBean subject = new SubjectDataBean();
        subject.setSubjectOID("SS_DEMO");
        subject.setStudyEventData(new ArrayList<>(List.of(event)));

        UpsertOnBean upsertOn = new UpsertOnBean();
        upsertOn.setDataEntryStarted(true);

        CRFDataPostImportContainer postImport = new CRFDataPostImportContainer();
        postImport.setStudyOID("S_DEMO");
        postImport.setUpsertOn(upsertOn);
        postImport.setSubjectData(new ArrayList<>(List.of(subject)));

        ODMContainer odm = new ODMContainer();
        odm.setCrfDataPostImportContainer(postImport);
        return odm;
    }

    private static final class NoopEventCrfPort implements ImportEventCrfPort {
        @Override
        public List<Object[]> findImportEventCrfsByEventSubjectVersion(
                int studyEventId, int studySubjectId, int crfVersionId) {
            return List.of();
        }

        @Override
        public List<Object[]> findImportEventCrfsByEventSubjectCrfId(
                int studyEventId, int studySubjectId, int crfVersionId) {
            return List.of();
        }

        @Override
        public Object[] createImportEventCrf(
                int studyEventId, int studySubjectId, int crfVersionId,
                int ownerId, String interviewerName, int statusId) {
            return new Object[]{0, crfVersionId, statusId};
        }
    }

    private static final class NoopItemDataPort implements ImportItemDataPort {
        @Override
        public void upsertImportItemData(
                int itemId, int eventCrfId, int ordinal, int ownerId, int statusId, String value) {
        }
    }
}

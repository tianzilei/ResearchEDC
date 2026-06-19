package org.researchedc.module.dataimport.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.Test;
import org.researchedc.bean.submit.crfdata.FormDataBean;
import org.researchedc.bean.submit.crfdata.ImportItemGroupDataBean;
import org.researchedc.bean.submit.crfdata.ODMContainer;
import org.researchedc.bean.submit.crfdata.StudyEventDataBean;
import org.researchedc.bean.submit.crfdata.SubjectDataBean;
import org.researchedc.module.dataimport.internal.support.ImportCompatibilitySupport;

class ImportCompatibilitySupportTest {

    @Test
    void odmMappingLocation_resolvesClasspathResource() throws Exception {
        String location = ImportCompatibilitySupport.odmMappingLocation();

        assertTrue(location.endsWith("/properties/cd_odm_mapping.xml"));
    }

    @Test
    void pageMessagesBundle_usesRequestedLocaleOrDefault() {
        ResourceBundle english = ImportCompatibilitySupport.pageMessagesBundle(Locale.ENGLISH);
        ResourceBundle fallback = ImportCompatibilitySupport.pageMessagesBundle(null);

        assertEquals(english.getString("your_current_study_is_not_the_same_as"),
            fallback.getString("your_current_study_is_not_the_same_as"));
    }

    @Test
    void importAdapter_parseOdm_usesModuleOwnedMappingSupport() throws Exception {
        Path fixture = Files.createTempFile("representative-crf-data", ".xml");
        Files.writeString(fixture, """
                <?xml version="1.0" encoding="UTF-8"?>
                <ODM xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1">
                  <ClinicalData StudyOID="S_DEMO">
                    <SubjectData SubjectKey="SS_DEMO">
                      <StudyEventData StudyEventOID="SE_BASELINE" StudyEventRepeatKey="1">
                        <FormData FormOID="F_VITALS_V1" OpenClinica:Status="data entry started">
                          <ItemGroupData ItemGroupOID="IG_VITALS" ItemGroupRepeatKey="1">
                            <ItemData ItemOID="I_WEIGHT" Value="70" TransactionType="Insert"/>
                          </ItemGroupData>
                        </FormData>
                      </StudyEventData>
                    </SubjectData>
                  </ClinicalData>
                </ODM>
                """);

        try {
            ImportCrfDataAdapter adapter = new ImportCrfDataAdapter(
                null, null, null, null, null, null, null, null, null, null, null);

            ODMContainer odm = adapter.parseOdm(fixture).odm();

            assertNotNull(odm.getCrfDataPostImportContainer());
            assertEquals("S_DEMO", odm.getCrfDataPostImportContainer().getStudyOID());
            SubjectDataBean subject = odm.getCrfDataPostImportContainer().getSubjectData().get(0);
            assertEquals("SS_DEMO", subject.getSubjectOID());
            StudyEventDataBean event = subject.getStudyEventData().get(0);
            assertEquals("SE_BASELINE", event.getStudyEventOID());
            FormDataBean form = event.getFormData().get(0);
            assertEquals("F_VITALS_V1", form.getFormOID());
            ImportItemGroupDataBean group = form.getItemGroupData().get(0);
            assertEquals("IG_VITALS", group.getItemGroupOID());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }
}

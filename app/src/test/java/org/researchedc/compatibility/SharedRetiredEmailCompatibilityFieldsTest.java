package org.researchedc.compatibility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.researchedc.domain.datamap.Study;
import org.researchedc.domain.user.UserAccount;

class SharedRetiredEmailCompatibilityFieldsTest {

    @Test
    void sharedUserAccountSetter_neutralizesRetiredEmailWrites() {
        UserAccount account = new UserAccount();

        account.setEmail("legacy@example.com");

        assertEquals("", account.getEmail());
    }

    @Test
    void sharedStudySetter_neutralizesRetiredFacilityContactEmailWrites() {
        Study study = new Study();

        study.setFacilityContactEmail("legacy@example.com");

        assertEquals("", study.getFacilityContactEmail());
    }

    @Test
    void sharedStudyConstructor_neutralizesRetiredFacilityContactEmailWrites() {
        Study study = new Study(
                1,
                null,
                null,
                null,
                null,
                "ID",
                null,
                "Name",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "legacy@example.com",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals("", study.getFacilityContactEmail());
    }
}

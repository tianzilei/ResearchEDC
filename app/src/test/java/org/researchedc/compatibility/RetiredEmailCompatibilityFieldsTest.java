package org.researchedc.compatibility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.study.entity.StudyEntity;

class RetiredEmailCompatibilityFieldsTest {

    @Test
    void userAccountEntitySetter_neutralizesRetiredEmailWrites() {
        UserAccountEntity entity = new UserAccountEntity();

        entity.setEmail("legacy@example.com");

        assertEquals("", entity.getEmail());
    }

    @Test
    void studyEntitySetter_neutralizesRetiredFacilityContactEmailWrites() {
        StudyEntity entity = new StudyEntity();

        entity.setFacilityContactEmail("legacy@example.com");

        assertEquals("", entity.getFacilityContactEmail());
    }
}

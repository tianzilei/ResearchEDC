package org.researchedc.compatibility;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.researchedc.module.identity.entity.UserAccountEntity;
import org.researchedc.module.study.entity.StudyEntity;

class RetiredEmailCompatibilityFieldsTest {

    @Test
    void moduleEntities_doNotExposeRetiredEmailFields() {
        assertNoMethod(UserAccountEntity.class, "getEmail");
        assertNoMethod(UserAccountEntity.class, "setEmail", String.class);

        assertNoMethod(StudyEntity.class, "getFacilityContactEmail");
        assertNoMethod(StudyEntity.class, "setFacilityContactEmail", String.class);
    }

    private void assertNoMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        assertThrows(NoSuchMethodException.class, () -> type.getMethod(name, parameterTypes));
    }
}

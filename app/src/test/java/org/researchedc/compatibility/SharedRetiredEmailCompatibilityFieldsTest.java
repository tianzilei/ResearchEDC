package org.researchedc.compatibility;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.researchedc.domain.datamap.Study;
import org.researchedc.domain.user.UserAccount;

class SharedRetiredEmailCompatibilityFieldsTest {

    @Test
    void sharedEntities_doNotExposeRetiredEmailFields() {
        assertNoMethod(UserAccount.class, "getEmail");
        assertNoMethod(UserAccount.class, "setEmail", String.class);

        assertNoMethod(Study.class, "getFacilityContactEmail");
        assertNoMethod(Study.class, "setFacilityContactEmail", String.class);
    }

    private void assertNoMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        assertThrows(NoSuchMethodException.class, () -> type.getMethod(name, parameterTypes));
    }
}

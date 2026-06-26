package org.researchedc.control.form.support;

import java.util.Set;

public final class FormStatusSupport {

    private static final Set<Integer> VALID_STATUS_IDS =
            Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);

    private FormStatusSupport() {
    }

    public static boolean isValidStatusId(int statusId) {
        return VALID_STATUS_IDS.contains(statusId);
    }
}

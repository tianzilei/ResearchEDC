package org.akaza.openclinica.module.subject.infrastructure;

import org.springframework.stereotype.Component;

@Component
public class LegacySubjectAdapter {

    public Object findLegacySubjectById(Integer id) {
        throw new UnsupportedOperationException(
            "Legacy DAO bridge not yet implemented — will connect to core SubjectDAO in future iteration");
    }
}

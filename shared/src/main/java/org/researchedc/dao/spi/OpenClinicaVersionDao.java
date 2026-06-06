package org.researchedc.dao.spi;

import org.researchedc.domain.OpenClinicaVersionBean;

public interface OpenClinicaVersionDao {
    OpenClinicaVersionBean findDefault();
    void saveOCVersionToDB(String openClinicaVersion);
    int deleteDefault();
}

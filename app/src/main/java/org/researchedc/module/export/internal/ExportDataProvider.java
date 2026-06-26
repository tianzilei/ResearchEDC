package org.researchedc.module.export.internal;

import java.util.List;
import org.researchedc.module.export.enums.OdmContractVersion;

public interface ExportDataProvider {

    OdmStudySnapshot getStudySnapshot(Integer studyId, OdmContractVersion contractVersion);

    List<OdmSubjectDataSnapshot> getSubjectData(Integer studyId);
}

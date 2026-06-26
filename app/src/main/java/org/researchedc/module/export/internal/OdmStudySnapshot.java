package org.researchedc.module.export.internal;

import java.util.List;

public record OdmStudySnapshot(
    String oid,
    String name,
    String description,
    String protocolName,
    String facilityName,
    String facilityContactEmail,
    String metaDataVersionOid,
    String metaDataVersionName
) {
}

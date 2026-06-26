package org.researchedc.module.export.internal;

import java.util.List;

public record OdmFormDataSnapshot(
    String formOid,
    String formRepeatKey,
    List<OdmItemGroupDataSnapshot> itemGroups
) {
}

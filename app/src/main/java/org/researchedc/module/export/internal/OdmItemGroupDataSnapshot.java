package org.researchedc.module.export.internal;

import java.util.List;

public record OdmItemGroupDataSnapshot(
    String itemGroupOid,
    String itemGroupRepeatKey,
    List<OdmItemDataSnapshot> items
) {
}

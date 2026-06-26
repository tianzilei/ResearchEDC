package org.researchedc.module.export.internal;

public record OdmItemDataSnapshot(
    String itemOid,
    String value,
    boolean isMonitored
) {
}

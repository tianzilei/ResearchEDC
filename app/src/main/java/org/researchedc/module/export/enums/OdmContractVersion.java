package org.researchedc.module.export.enums;

public enum OdmContractVersion {

    OC2_0_COMPAT("OC2-0", "Legacy-compatible schema retaining deprecated FacilityContactEmail"),
    OC2_1("OC2-1", "Email-free schema removing FacilityContactEmail");

    private final String schemaVersion;
    private final String description;

    OdmContractVersion(String schemaVersion, String description) {
        this.schemaVersion = schemaVersion;
        this.description = description;
    }

    public String getSchemaVersion() { return schemaVersion; }
    public String getDescription() { return description; }
}

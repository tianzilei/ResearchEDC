package org.researchedc.module.export.service;

import java.util.Map;
import org.researchedc.module.export.enums.OdmContractVersion;
import org.springframework.stereotype.Component;

@Component
public class OdmSchemaResourceResolver {

    private static final String PROPERTIES_PREFIX = "properties/";

    private static final Map<OdmContractVersion, SchemaPaths> SCHEMA_MAP = Map.of(
        OdmContractVersion.OC2_0_COMPAT, new SchemaPaths(
            PROPERTIES_PREFIX + "OpenClinica-ODM1-3-0-OC2-0.xsd",
            PROPERTIES_PREFIX + "OpenClinica-ToODM1-3-0-OC2-0.xsd",
            PROPERTIES_PREFIX + "OpenClinica-ODM1-3-0-OC2-0-foundation.xsd"
        ),
        OdmContractVersion.OC2_1, new SchemaPaths(
            PROPERTIES_PREFIX + "OpenClinica-ODM1-3-0-OC2-1.xsd",
            PROPERTIES_PREFIX + "OpenClinica-ToODM1-3-0-OC2-1.xsd",
            PROPERTIES_PREFIX + "OpenClinica-ODM1-3-0-OC2-1-foundation.xsd"
        )
    );

    public SchemaPaths resolve(OdmContractVersion version) {
        return SCHEMA_MAP.get(version);
    }

    public record SchemaPaths(String mainXsd, String toOdmXsd, String foundationXsd) {
    }
}

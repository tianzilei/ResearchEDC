@org.springframework.modulith.ApplicationModule(
    displayName = "Dashboard",
    allowedDependencies = {
        "identity::service", "identity::dto",
        "study::service", "study::dto",
        "event::service",
        "audit::service", "audit::dto", "audit::enums",
        "discrepancynote::service"
    }
)
package org.researchedc.module.dashboard;

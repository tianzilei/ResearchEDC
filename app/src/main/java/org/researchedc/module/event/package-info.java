@org.springframework.modulith.ApplicationModule(
  displayName = "Event",
  allowedDependencies = {"audit::service", "audit::enums", "dataimport::service", "dataimport::dto"}
)
package org.researchedc.module.event;

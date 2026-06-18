@org.springframework.modulith.ApplicationModule(
  displayName = "Event",
  allowedDependencies = {"audit::service", "audit::enums", "dataimport::service"}
)
package org.researchedc.module.event;

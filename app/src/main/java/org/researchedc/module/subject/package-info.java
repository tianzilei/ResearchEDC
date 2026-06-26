@org.springframework.modulith.ApplicationModule(
  displayName = "Subject",
  allowedDependencies = {"audit::service", "audit::enums", "event::service", "event::dto", "dataimport::service", "dataimport::dto"}
)
package org.researchedc.module.subject;

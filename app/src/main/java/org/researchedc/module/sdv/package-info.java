@org.springframework.modulith.ApplicationModule(
  displayName = "SDV",
  allowedDependencies = {
    "audit::service", "audit::enums",
    "event::repository", "event::entity",
    "subject::repository", "subject::entity"
  }
)
package org.researchedc.module.sdv;

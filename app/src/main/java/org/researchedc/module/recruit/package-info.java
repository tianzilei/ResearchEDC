@org.springframework.modulith.ApplicationModule(
  displayName = "Recruit",
  allowedDependencies = {
    "audit::service", "audit::enums",
    "subject::service", "subject::dto"
  }
)
package org.researchedc.module.recruit;

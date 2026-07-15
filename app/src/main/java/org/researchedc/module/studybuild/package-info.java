@org.springframework.modulith.ApplicationModule(
  displayName = "Study Build",
  allowedDependencies = {
    "audit::service", "audit::enums",
    "study::service", "study::dto"
  }
)
package org.researchedc.module.studybuild;

@org.springframework.modulith.ApplicationModule(
  displayName = "Participant Access",
  allowedDependencies = {
    "audit::service", "audit::enums",
    "subject::repository", "subject::entity"
  }
)
package org.researchedc.module.participantaccess;

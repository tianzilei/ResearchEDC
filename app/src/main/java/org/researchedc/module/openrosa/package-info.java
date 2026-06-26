@org.springframework.modulith.ApplicationModule(
  displayName = "OpenRosa",
  allowedDependencies = {
    "audit::service",
    "audit::enums",
    "crf::entity",
    "crf::repository",
    "study::entity",
    "study::repository",
    "subject::entity",
    "subject::repository",
    "event::entity",
    "event::repository",
    "datacapture::entity",
    "datacapture::repository",
    "identity::entity",
    "identity::repository"
  }
)
package org.researchedc.module.openrosa;

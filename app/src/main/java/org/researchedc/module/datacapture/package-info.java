@org.springframework.modulith.ApplicationModule(
  displayName = "Data Capture",
  allowedDependencies = {
    "audit::service", "audit::enums",
    "event::repository", "event::entity",
    "subject::repository", "subject::entity",
    "crf::repository", "crf::entity",
    "rule::repository", "rule::entity"
  }
)
package org.researchedc.module.datacapture;

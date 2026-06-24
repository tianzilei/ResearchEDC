@org.springframework.modulith.ApplicationModule(
  displayName = "Export",
  allowedDependencies = {
    "study::repository", "study::entity",
    "subject::repository", "subject::entity",
    "event::repository", "event::entity",
    "datacapture::repository", "datacapture::entity",
    "crf::repository", "crf::entity"
  }
)
package org.researchedc.module.export;

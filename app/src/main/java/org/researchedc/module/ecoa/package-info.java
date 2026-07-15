@org.springframework.modulith.ApplicationModule(
  displayName = "eCOA",
  allowedDependencies = {
    "audit::service", "audit::enums",
    "participantaccess::service", "participantaccess::dto",
    "task::service", "task::dto", "task::enums"
  }
)
package org.researchedc.module.ecoa;

@org.springframework.modulith.ApplicationModule(
  displayName = "Analytics",
  allowedDependencies = {
    "subject::repository",
    "recruit::service", "recruit::dto", "recruit::enums",
    "ecoa::service", "ecoa::dto",
    "dashboard::service", "dashboard::dto"
  }
)
package org.researchedc.module.analytics;

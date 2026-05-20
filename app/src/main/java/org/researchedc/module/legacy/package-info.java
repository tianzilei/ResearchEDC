@org.springframework.modulith.ApplicationModule(
  displayName = "Legacy Gateway",
  allowedDependencies = {"study::service", "study::dto", "subject::service", "subject::dto", "crf::service", "crf::entity"}
)
package org.researchedc.module.legacy;

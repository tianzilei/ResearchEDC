@org.springframework.modulith.ApplicationModule(
  displayName = "Legacy Gateway",
  allowedDependencies = {"study::service", "study::dto", "subject::service", "subject::dto", "crf::service", "crf::entity", "rule::service", "rule::entity", "dataset::service", "dataset::entity", "filter::service", "filter::entity", "subjectgroup::service", "subjectgroup::entity", "discrepancynote::service", "discrepancynote::entity", "identity", "audit::service", "audit::enums", "dataimport::dto", "dataimport::service"}
)
package org.researchedc.module.legacy;

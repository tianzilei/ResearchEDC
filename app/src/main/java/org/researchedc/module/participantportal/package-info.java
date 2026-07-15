@org.springframework.modulith.ApplicationModule(
  displayName = "Participant Portal",
  allowedDependencies = {
    "participantaccess::service", "participantaccess::dto",
    "ecoa::service", "ecoa::dto", "ecoa::enums",
    "econsent::service", "econsent::dto", "econsent::enums"
  }
)
package org.researchedc.module.participantportal;

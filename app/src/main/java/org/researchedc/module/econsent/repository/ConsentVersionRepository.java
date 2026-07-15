package org.researchedc.module.econsent.repository;

import java.util.List;
import org.researchedc.module.econsent.entity.ConsentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentVersionRepository extends JpaRepository<ConsentVersion, Long> {
    List<ConsentVersion> findByTemplateIdOrderByCreatedDateDesc(Long templateId);
}

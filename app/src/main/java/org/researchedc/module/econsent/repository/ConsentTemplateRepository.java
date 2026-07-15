package org.researchedc.module.econsent.repository;

import java.util.List;
import org.researchedc.module.econsent.entity.ConsentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentTemplateRepository extends JpaRepository<ConsentTemplate, Long> {
    List<ConsentTemplate> findByStudyIdOrderByNameAsc(Integer studyId);
}

package org.researchedc.module.subject.repository;

import java.util.List;
import java.util.Optional;
import org.researchedc.module.subject.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, Integer> {

    Optional<SubjectEntity> findByUniqueIdentifier(String uniqueIdentifier);

    List<SubjectEntity> findByUniqueIdentifierContainingIgnoreCase(String query);
}
